/*
 * Copyright (C) 2016 Ordnance Survey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.os.vt.mbtiles;

import com.github.davidmoten.rx.jdbc.Database;
import com.github.davidmoten.rx.jdbc.QuerySelect;
import com.github.davidmoten.rx.jdbc.QueryUpdate;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import uk.os.vt.Entry;
import uk.os.vt.JsonUtil;
import uk.os.vt.Metadata;
import uk.os.vt.MetadataProvider;
import uk.os.vt.Storage;
import uk.os.vt.common.CompressUtil;

public class StorageImpl implements Storage, MetadataProvider {

  public static final class Builder {

    private final File file;
    private boolean createIfNotExist;

    public Builder(String filePath) {
      this.file = new File(filePath);
    }

    public Builder(File file) {
      this.file = file;
    }

    public Builder createIfNotExist() {
      createIfNotExist = true;
      return this;
    }

    /**
     * Build an MBTiles storage.
     *
     * @return the storage
     * @throws IOException thrown if IO error occurs
     */
    public StorageImpl build() throws IOException {
      final boolean isInitializationRequired = createIfNotExist && !file.exists();
      if (isInitializationRequired) {
        try {
          Util.initialise(file);
        } catch (final IOException ex) {
          throw new IOException("cannot initiaize mbtile", ex);
        }
      }

      if (!file.exists()) {
        throw new IllegalStateException("file does not exist");
      }
      return new StorageImpl(file);
    }
  }


  private static final Logger LOG = LoggerFactory.getLogger(StorageImpl.class.getSimpleName());

  private final Database dataSource;
  private final Connection connection;
  private final boolean isError;

  // cache
  private Observable<VectorTileConfig> configObservable;

  private StorageImpl(File file) {
    Connection connection = null;
    Database datasource = null;
    try {
      connection = getConnection(file);
      datasource = Database.from(connection);
    } catch (final Exception ex) {
      LOG.error("problem establishing a connection", ex);
    }
    this.dataSource = datasource;
    this.connection = connection;
    isError = dataSource == null || connection == null;
  }

  @Override
  public void close() throws Exception {
    final boolean isConnection = connection != null;
    if (isConnection) {
      connection.close();
    }

    final boolean isDatabase = dataSource != null;
    if (isDatabase) {
      dataSource.close();
    }
  }

  @Override
  public Observable<Entry> getEntry(int zoom, int col, int row) {
    return get(dataSource
        .select("SELECT zoom_level, tile_column, tile_row, tile_data " + "FROM tiles "
            + "WHERE zoom_level = ? " + "AND tile_column = ? " + "AND tile_row = ?")
        .parameter(zoom).parameter(col).parameter(flipY(row, zoom)));
  }

  @Override
  public Observable<Entry> getEntries() {
    return get(
        dataSource.select("SELECT zoom_level, tile_column, tile_row, tile_data " + "FROM tiles"));
  }

  @Override
  public Observable<Entry> getEntries(int zoom) {
    return get(dataSource.select("SELECT zoom_level, tile_column, tile_row, tile_data "
        + "FROM tiles " + "WHERE zoom_level = ?").parameter(zoom));
  }

  @Override
  public Observable<Integer> getMaxZoomLevel() {
    return queryConfig().map(VectorTileConfig::getMaxZoom);
  }

  @Override
  public Observable<Integer> getMinZoomLevel() {
    return queryConfig().map(VectorTileConfig::getMinZoom);
  }

  @Override
  public void putEntries(Observable<Entry> entries) {
    final String insert =
        "INSERT OR REPLACE INTO TILES(zoom_level, tile_column, tile_row, tile_data)"
            + " values (?, ?, ?, ?);";

    final Observable<Object> params = entries.concatMap(entry -> {
      byte[] compressedMvt;
      try {
        compressedMvt = CompressUtil.getCompressedAsGzip(entry.getVector());
      } catch (final IOException ex) {
        throw Exceptions.propagate(ex);
      }

      return Observable.<Object>just(entry.getZoomLevel(), entry.getColumn(),
          flipY(entry.getRow(), entry.getZoomLevel()), compressedMvt);
    })
        // source: https://github.com/davidmoten/rxjava-jdbc/pull/46/files
        .toList()
        .concatMap(Observable::from);
    dataSource.update(insert).parameters(params).execute();
    dataSource.commit();
  }

  @Override
  public Single<Metadata> generateDefault() {
    return Single.zip(queryMetadata(), queryConfig().toSingle(), (metadata, config) -> {
      final JSONObject tileJson = new JSONObject();
      if (metadata.containsKey("json")) {
        try {
          final JSONObject json = new JSONObject(metadata.get("json"));
          tileJson.put("vector_layers", json.getJSONArray("vector_layers"));
        } catch (final JSONException ex) {
          LOG.error("problem generating JSON for metadata", ex);
        }
      }

      try {
        addMetadataToTileJson(metadata, tileJson);
      } catch (final JSONException ex) {
        LOG.error("problem generating metadata", ex);
      }

      final Metadata.Builder builder = new Metadata.Builder();
      builder.setMinZoom(config.getMinZoom());
      builder.setMaxZoom(config.getMaxZoom());
      builder.setTileJson(tileJson);
      return builder.build();
    }).onErrorReturn(new Func1<Throwable, Metadata>() {
      @Override
      public Metadata call(Throwable throwable) {
        // TODO REMOVE THIS SHOCKING HACK!
        return new Metadata.Builder().build();
      }
    });
  }

  @Override
  public Observable<Metadata> getMetadata() {
    final String query = "SELECT name, value FROM metadata";
    return dataSource.select(query).get(rs -> {
      final Map<String, String> metadata = new TreeMap<>();
      // TODO consider QA on why design wasn't rs.next() with cursor starting at -1
      while (rs.getRow() != 0) {
        metadata.put(rs.getString("name"), rs.getString("value"));
        rs.next();
      }
      return metadata;
    }).map(map -> {
      final Metadata.Builder metadata = new Metadata.Builder();
      try {
        // TODO handle any extra metadata! i.e. more key values! Parse as JSON? Force key value?
        for (Map.Entry<String, String> entry : map.entrySet()) {
          String key = entry.getKey();
          if (key.equals("type") || key.equals("version")) {
            // Warning: if the user specified 'type' or 'version' attributes in the TileJson then
            // those
            // attributes would be ignored.
            // The above is the not the issue per se _but_ rather we should consider adding this
            // elsewhere, e.g. in the json field
            continue;
          }
          final boolean ignore = key.equals("json");
          if (!ignore) {
            String value = map.get(key).trim();

            if (!value.isEmpty() && key.equals("center") || key.equals("bounds")) {
              value = "[" + value + "]";
            }

            if (!value.isEmpty() && value.substring(0, 1).equals("[")) {
              metadata.setJson(key, new JSONArray(value));
            } else if (!value.isEmpty() && value.substring(0, 1).equals("{")) {
              metadata.setJson(key, new JSONObject(value));
            } else {
              final boolean isInteger = value.matches("^-?\\d+$");
              // see
              // http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
              final boolean isFloat = !isInteger && value.matches("[+-]([0-9]*[.])?[0-9]+");
              // see
              // http://stackoverflow.com/questions/12643009/regular-expression-for-floating-point-numbers

              if (isInteger) {
                metadata.setJson(key, Long.parseLong(value));
              } else if (isFloat) {
                metadata.setJson(key, Double.parseDouble(value));
              } else {
                // default to string
                metadata.setJson(key, value);
              }
            }
          }
        }

        if (map.containsKey("json")) {
          final JSONObject json = new JSONObject(map.get("json"));
          if (json.has("vector_layers")) {
            metadata.setJson("vector_layers", json.getJSONArray("vector_layers"));
          }
        }
      } catch (final JSONException ex) {
        LOG.error("problem", ex);
      }
      return metadata.build();
    });
  }

  @Override
  public Subscription putMetadata(Single<Metadata> metadata) {
    return metadata.subscribe(m -> {
      final String insert = "INSERT OR REPLACE INTO metadata (name, value) VALUES (?, ?);";

      final JSONObject tilejson = m.getTileJson();
      final JSONArray fieldNames = tilejson.names();

      final List<Observable<Object>> rows = new ArrayList<>();

      for (int i = 0; i < fieldNames.length(); i++) {
        final String key = JsonUtil.getStringIgnoreErrors(i, fieldNames, "");
        final boolean isKeyExcluded =
            key.isEmpty() || key.equals("vector_layers") || key.equals("tilejson");
        if (isKeyExcluded) {
          continue;
        }
        try {
          final String value = tilejson.getString(key);
          if (key.equals("center") || key.equals("bounds")) {
            final Pattern pattern = Pattern.compile("^\\[(.*)\\]$");
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
              final String toCommit = matcher.group(1);
              rows.add(Observable.<Object>just(key, toCommit));
            }
          } else if (value != null && !value.isEmpty()) {
            rows.add(Observable.<Object>just(key, value));
          }
        } catch (final JSONException ex) {
          LOG.error("problem", ex);
        }
      }

      JSONObject vectorLayerJson = new JSONObject();
      if (tilejson.has("vector_layers")) {
        try {
          final JSONArray vectorLayers = tilejson.getJSONArray("vector_layers");
          vectorLayerJson.put("vector_layers", new JSONArray(vectorLayers.toString()));
        } catch (final JSONException ex) {
          LOG.error("problem", ex);
          vectorLayerJson = null;
        }
      }

      rows.add(Observable.<Object>just("format", "pbf"));
      rows.add(Observable.<Object>just("version", "1.1"));
      rows.add(Observable.<Object>just("type", "overlay"));
      rows.add(Observable.just("json", vectorLayerJson));

      QueryUpdate.Builder builder = dataSource.update(insert);
      for (final Observable<Object> row : rows) {
        builder = builder.parameters(row);
      }
      builder.execute();

      dataSource.commit();
    });
  }

  private static void addMetadataToTileJson(Map<String, String> metadata, JSONObject tileJson)
      throws JSONException {
    for (final Map.Entry<String, String> entry : metadata.entrySet()) {
      String key = entry.getKey();
      if (key.equals("version") || key.equals("json")) {
        continue;
      } else if (key.equals("bounds") || key.equals("center")) {
        tileJson.put(key, new JSONArray("[" + entry.getValue() + "]"));
      } else {
        tileJson.put(key, entry.getValue());
      }
    }
  }

  private synchronized Single<HashMap<String, String>> queryMetadata() {
    final URL url = Resources.getResource("metadata_key_value.sql");
    String query;
    try {
      query = Resources.toString(url, Charsets.UTF_8);
    } catch (final IOException ex) {
      return Single.error(ex);
    }
    return dataSource.select(query).get(rs -> {
      final HashMap<String, String> metadata = new LinkedHashMap<>();

      while (rs.getRow() != 0) {
        metadata.put(rs.getString("name"), rs.getString("value"));
        rs.next();
      }
      return metadata;
    }).toSingle();
  }

  // TODO remove
  private synchronized Observable<VectorTileConfig> queryConfig() {
    final boolean isPendingQuery = configObservable != null;
    if (!isPendingQuery) {
      final URL url = Resources.getResource("metadata_raw.sql");
      String query;
      try {
        query = Resources.toString(url, Charsets.UTF_8);
      } catch (final IOException ex) {
        return Observable.error(ex);
      }
      configObservable = dataSource.select(query)
          .get(rs -> new VectorTileConfig(rs.getInt("min_zoom"), rs.getInt("max_zoom"),
              rs.getInt("max_zoom_minx"), rs.getInt("max_zoom_miny"), rs.getInt("max_zoom_maxx"),
              rs.getInt("max_zoom_maxy")))
          .replay().autoConnect();
    }
    return configObservable;
  }

  /**
   * Get an Entry observable.
   *
   * @param builder to describe the builder
   * @return all entries for the given builder
   */
  private Observable<Entry> get(QuerySelect.Builder builder) {
    if (isError) {
      return Observable.empty();
    } else {
      return builder.get(rs -> {
        byte[] uncompressed;
        try {
          final byte[] compressedTileData = rs.getBytes("tile_data");
          uncompressed = CompressUtil.getUncompressedFromGzip(compressedTileData);
        } catch (final IOException ex) {
          throw Exceptions.propagate(ex);
        }
        return new Entry(rs.getInt("zoom_level"), rs.getInt("tile_column"),
            flipY(rs.getInt("tile_row"), rs.getInt("zoom_level")), uncompressed);
      });
    }
  }

  private static Connection getConnection(File file) throws SQLException {
    return getConnection(file.getAbsolutePath());
  }

  private static Connection getConnection(String fileLocation) throws SQLException {
    LOG.info("DB file location " + fileLocation);
    return DriverManager.getConnection("jdbc:sqlite:" + fileLocation);
  }

  private static int flipY(int row, int zoom) {
    return (int) (Math.pow(2, zoom) - row - 1);
  }

  private static class VectorTileConfig {

    private final int maxZoom;
    private final int minZoom;
    private final int maxZoomMinX;
    private final int maxZoomMinY;
    private final int maxZoomMaxX;
    private final int maxZoomMaxY;

    public VectorTileConfig(int minZoom, int maxZoom, int maxZoomMinX, int maxZoomMinY,
        int maxZoomMaxX, int maxZoomMaxY) {
      this.minZoom = minZoom;
      this.maxZoom = maxZoom;
      this.maxZoomMaxX = maxZoomMaxX;
      this.maxZoomMaxY = maxZoomMaxY;
      this.maxZoomMinX = maxZoomMinX;
      this.maxZoomMinY = maxZoomMinY;
    }

    double[] getExtentAsLatLon() {
      // TODO verify
      LOG.info("WARNING - check this!!!"); // TODO
      return new double[] {tile2lat(maxZoomMinY, maxZoom), tile2lat(maxZoomMaxY + 1, maxZoom),
          tile2lon(maxZoomMinX, maxZoom), tile2lon(maxZoomMaxX + 1, maxZoom)};
    }

    int getMaxZoom() {
      return maxZoom;
    }

    int getMinZoom() {
      return minZoom;
    }

    double tile2lon(int col, int zoom) {
      return col / Math.pow(2.0, zoom) * 360.0 - 180;
    }

    double tile2lat(int row, int zoom) {
      final double n = Math.PI - (2.0 * Math.PI * row) / Math.pow(2.0, zoom);
      return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
  }
}
