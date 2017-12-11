/*
 * Copyright (C) 2017 Weather Decision Technologies
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

package uk.os.vt.mvt.build;

import java.util.Iterator;
import uk.os.vt.mvt.VectorTile;
import uk.os.vt.mvt.encoding.MvtValue;

/**
 * Utility methods for building Mapbox-Vector-Tile layers.
 */
public final class MvtLayerBuild {

  /**
   * Create a new {@link uk.os.vt.mvt.VectorTile.Tile.Layer.Builder} instance with
   * initialized version, name, and extent metadata.
   *
   * @param layerName      name of the layer
   * @param mvtLayerParams tile creation parameters
   * @return new layer builder instance with initialized metadata.
   */
  public static VectorTile.Tile.Layer.Builder newLayerBuilder(String layerName,
                                                              MvtLayerParams mvtLayerParams) {
    final VectorTile.Tile.Layer.Builder layerBuilder = VectorTile.Tile.Layer.newBuilder();
    layerBuilder.setVersion(2);
    layerBuilder.setName(layerName);
    layerBuilder.setExtent(mvtLayerParams.extent);

    return layerBuilder;
  }

  /**
   * Modifies {@code layerBuilder} to contain properties from {@code layerProps}.
   *
   * @param layerBuilder layer builder to write to
   * @param layerProps   properties to write
   */
  public static void writeProps(VectorTile.Tile.Layer.Builder layerBuilder,
                                MvtLayerProps layerProps) {

    // Add keys
    layerBuilder.addAllKeys(layerProps.getKeys());

    // Add values
    final Iterable<Object> vals = layerProps.getVals();

    for (Iterator<Object> iterator = vals.iterator(); iterator.hasNext();) {
      layerBuilder.addValues(MvtValue.toValue(iterator.next()));
    }
  }
}
