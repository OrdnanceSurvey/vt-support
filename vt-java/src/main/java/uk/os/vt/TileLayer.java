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

package uk.os.vt;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IGeometryFilter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TagKeyValueMapConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TileGeomResult;
import com.wdtinc.mapbox_vector_tile.adapt.jts.UserDataKeyValueMapConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;

public class TileLayer {

  private static final GeometryFactory GEOMETRY_FACORY = new GeometryFactory();
  private static final Logger LOG = LoggerFactory.getLogger(TileLayer.class.getSimpleName());
  private static final int TILE_MEASUREMENT_SPACE = 4096;

  private final String name;
  private final int min;
  private final int max;
  private final Storage storage;
  private final String attribution;

  public static final class Builder {

    private Storage storage;
    private int maxZoom = 0;
    private int minZoom = 22;
    private String attribution = "undefined";
    private String name = "undefined";

    public Builder setStorage(Storage storage) {
      this.storage = storage;
      return this;
    }

    public Builder setMaxZoom(int value) {
      maxZoom = value;
      return this;
    }

    public Builder setMinZoom(int value) {
      minZoom = value;
      return this;
    }

    public Builder setAttribution(String value) {
      attribution = value;
      return this;
    }

    public Builder setName(String value) {
      name = value;
      return this;
    }

    /**
     * Build the tile layer.
     *
     * @return the tile layer
     */
    public TileLayer build() {
      Metadata.Layer mvtLayer = new Metadata.Layer.Builder()
          .setId(name)
          .setMinZoom(minZoom)
          .setMaxZoom(maxZoom).build();
      Metadata metadata = new Metadata.Builder()
          .copyMetadata(storage.generateDefault().toBlocking().value())
          .addLayer(mvtLayer)
          .build();
      storage.putMetadata(Single.just(metadata));
      return new TileLayer(name, minZoom, maxZoom, storage, attribution);
    }
  }

  private TileLayer(String name, int min, int max, Storage storage, String attribution) {
    this.name = name;
    this.min = min;
    this.max = max;
    this.storage = storage;
    this.attribution = attribution;
  }

  public void addPoint(double[] latlon) {
    addPoint(GEOMETRY_FACORY.createPoint(new Coordinate(latlon[1], latlon[0])));
  }

  /**
   * Add a point.
   *
   * @param point defined with lat / lon values
   */
  public void addPoint(Point point) {
    for (int z = min; z < max; z++) {
      int[] coordinates = CoordinateConversion.toTileCoordinates(point, z, TILE_MEASUREMENT_SPACE);

      Observable<Entry> update = storage.getEntry(coordinates[0], coordinates[1], coordinates[2])
          .firstOrDefault(
              new Entry(coordinates[0], coordinates[1], coordinates[2], createEmptyPicture()))
          .map(existing -> {
            try {
              List<Geometry> geometries =
                  MvtReader.loadMvt(new ByteArrayInputStream(existing.getVector()),
                      GEOMETRY_FACORY, new TagKeyValueMapConverter());

              geometries.add(createPoint(coordinates));
              byte[] newPicture = createPicture(geometries);
              return new Entry(existing.getZoomLevel(), existing.getColumn(), existing.getRow(),
                  newPicture);
            } catch (IOException ioException) {
              LOG.error("problem adding point to existing vector tile", ioException);
              throw Exceptions.propagate(ioException);
            }
          });

      storage.putEntries(update);
    }
  }

  private Point createPoint(int[] coordinates) {
    return GEOMETRY_FACORY.createPoint(new Coordinate(coordinates[3], coordinates[4]));
  }

  private byte[] createEmptyPicture() {
    LOG.info("making an empty picture!!!");
    return createPicture(new ArrayList<>());
  }

  private byte[] createPicture(List<Geometry> geometries) {
    Picture.Builder mvt = new Picture.Builder().setLayer(name);
    for (Geometry geometry : geometries) {
      mvt.add(geometry);
    }
    return mvt.build();
  }

  private static class Picture {

    // Default MVT parameters
    private static final MvtLayerParams DEFAULT_MVT_PARAMS = new MvtLayerParams();

    // Do not filter tile geometry
    private static final IGeometryFilter ACCEPT_ALL_FILTER = geometry -> true;

    public static class Builder {
      private String activeLayer = "default";

      private Map<String, List<Geometry>> layers = new HashMap<>();

      public Builder() {}

      public Builder setLayer(String layerName) {
        activeLayer = layerName;
        return this;
      }

      private List<Geometry> getActiveLayer() {
        boolean isDefined = layers.containsKey(activeLayer);
        if (!isDefined) {
          layers.put(activeLayer, new ArrayList<>());
        }
        return layers.get(activeLayer);
      }

      public Builder add(Geometry geometry) {
        getActiveLayer().add(geometry);
        return this;
      }

      public byte[] build() {
        // Build MVT
        final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

        for (Map.Entry<String, List<Geometry>> layer : layers.entrySet()) {
          String layerName = layer.getKey();
          List<Geometry> layerGeoms = layer.getValue();

          TileGeomResult tileGeom = new TileGeomResult(layerGeoms, layerGeoms);

          // Create MVT layer
          final VectorTile.Tile.Layer.Builder layerBuilder =
              MvtLayerBuild.newLayerBuilder(layerName, DEFAULT_MVT_PARAMS);
          final MvtLayerProps layerProps = new MvtLayerProps();
          final UserDataKeyValueMapConverter userData = new UserDataKeyValueMapConverter();

          // MVT tile geometry to MVT features
          final List<VectorTile.Tile.Feature> features =
              JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, userData);
          layerBuilder.addAllFeatures(features);
          MvtLayerBuild.writeProps(layerBuilder, layerProps);

          // Build MVT layer
          final VectorTile.Tile.Layer mvtLayer = layerBuilder.build();
          tileBuilder.addLayers(mvtLayer);
        }

        // Build MVT
        return tileBuilder.build().toByteArray();
      }
    }
  }
}
