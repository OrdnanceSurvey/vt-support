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

package uk.os.vt.fluent;

import com.vividsolutions.jts.geom.Geometry;
import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtEncoder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import uk.os.vt.Entry;
import uk.os.vt.Key;
import uk.os.vt.Metadata;
import uk.os.vt.Storage;

public class DataSource {

  private static final Logger LOG = LoggerFactory.getLogger(DataSource.class);

  private final Storage storage;
  private final Set<FluentLayer> layers = new LinkedHashSet<>();
  private final Metadata schema;

  private FluentLayer activeLayer;
  private int defaultNewLayerMax = 5;

  public DataSource(Storage storage) {
    this(storage, new Metadata.Builder().build());
    LOG.warn("No schema!  Should you be using a schema?!");
  }

  /**
   * Construct the a datasource to easily work with a tileset.
   *
   * @param storage to use for this datasource
   * @param schema the data definition to use with this datasource
   */
  public DataSource(Storage storage, Metadata schema) {
    this.storage = storage;
    this.schema = schema;
    for (Metadata.Layer l : schema.getLayers()) {
      layers.add(new FluentLayer(l, new ArrayList<>()));
    }
  }

  /**
   * Specify the layer to use.
   *
   * @param layerName the layer for further operations to use
   * @return this datasource
   */
  public DataSource using(String layerName) {
    FluentLayer use = null;
    for (FluentLayer layer : layers) {
      if (layer.getName().equals(layerName)) {
        use = layer;
        break;
      }
    }
    if (use == null) {
      LOG.warn(String.format("creating layer '%s'.  Should it be in the schema?", layerName));
      use = new FluentLayer(layerName).setMaxZoom(defaultNewLayerMax);
      layers.add(use);
    }
    activeLayer = use;
    return this;
  }

  public DataSource add(Geometry geometry) {
    activeLayer.add(geometry);
    return this;
  }

  /**
   * Update backing storage with changes.
   *
   * @return this datasource
   */
  public DataSource commit() {
    Map<Key, JtsMvt> fragments = Fragmenter.fragment(layers);
    commit(fragments);
    updateMetadata();
    return this;
  }

  private void commit(Map<Key, JtsMvt> fragments) {
    Set<Map.Entry<Key, JtsMvt>> entrySet = fragments.entrySet();
    Iterator<Map.Entry<Key, JtsMvt>> iterator = entrySet.iterator();

    List<Entry> entries = new ArrayList<>();

    while (iterator.hasNext()) {
      Map.Entry<Key, JtsMvt> item = iterator.next();
      Key key = item.getKey();
      int zoom = key.getZ();
      int column = key.getX();
      int row = key.getY();

      byte[] bytes = MvtEncoder.encode(item.getValue());
      entries.add(new Entry(zoom, column, row, bytes));
    }

    rx.Observable<Entry> updated = rx.Observable.from(entries).flatMap(new Func1<Entry,
        rx.Observable<List<Entry>>>() {
      @Override
      public rx.Observable<List<Entry>> call(Entry entry) {
        return storage.getEntry(entry.getZoomLevel(), entry.getColumn(), entry.getRow())
            .map(new Func1<Entry, List<Entry>>() {
              @Override
              public List<Entry> call(Entry newEntry) {
                return Arrays.asList(entry, newEntry);
              }
            }).defaultIfEmpty(Arrays.asList(null, entry));
      }
    }).map(new Func1<List<Entry>, Entry>() {
      @Override
      public Entry call(List<Entry> entries) {
        Entry oldEntry = entries.get(0);
        Entry newEntry = entries.get(1);

        if (oldEntry == null) {
          // return early - not appending
          return newEntry;
        }

        Entry result;

        try {
          JtsMvt toReturn;
          JtsMvt oldMvt = MvtDecoder.decode(oldEntry.getVector());
          JtsMvt newMvt = MvtDecoder.decode(newEntry.getVector());

          // add existing layers
          toReturn = new JtsMvt(oldMvt.getLayers());
          //TODO toReturn.addLayers(oldMvt.getLayers());

          // apply new data
          for (JtsLayer layer : newMvt.getLayers()) {
            if (toReturn.getLayer(layer.getName()) == null) {
              toReturn.getLayersByName().put(layer.getName(), layer);
              //TODO toReturn.addLayer(layer);
            } else {
              toReturn.getLayer(layer.getName()).getGeometries().addAll(layer.getGeometries());
              //TODO toReturn.getLayer(layer.getName()).addAll(layer.getGeometries());
            }
          }

          // create replacement vector tile
          result = new Entry(oldEntry.getZoomLevel(), oldEntry.getColumn(), oldEntry.getRow(),
              MvtEncoder.encode(toReturn));
        } catch (IOException exception) {
          throw Exceptions.propagate(exception);
        }

        if (result == null) {
          throw Exceptions.propagate(new IllegalStateException("problem creating new vector tile"));
        }

        return result;
      }
    }).doOnError(new Action1<Throwable>() {
      @Override
      public void call(Throwable throwable) {
        throwable.printStackTrace();
        System.out.println("Yikes!");
      }
    });
    storage.putEntries(updated);
  }

  private void updateMetadata() {
    storage.getMetadata().defaultIfEmpty(new Metadata.Builder(schema)
        .setAttribution(System.getProperty("user.name")).build())
        .subscribe(new Action1<Metadata>() {
          @Override
          public void call(Metadata metadata) {
            Metadata.Builder newMetadata = new Metadata.Builder(metadata);
            for (FluentLayer layer : layers) {
              if (!isLayerInMetadata(metadata, layer.getName())) {
                newMetadata.addLayer(layer.getMetadata());
              }
            }
            storage.putMetadata(Single.just(newMetadata.build()));
          }
        });
  }

  private boolean isLayerInMetadata(Metadata metadata, String layerName) {
    for (uk.os.vt.Metadata.Layer l : metadata.getLayers()) {
      if (layerName.equals(l.getId())) {
        return true;
      }
    }
    return false;
  }
}
