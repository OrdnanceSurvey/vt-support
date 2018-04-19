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

package uk.os.vt.mvt.adapt.jts;

import java.util.Collection;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import uk.os.vt.mvt.VectorTile;
import uk.os.vt.mvt.adapt.jts.model.JtsLayer;
import uk.os.vt.mvt.adapt.jts.model.JtsMvt;
import uk.os.vt.mvt.build.MvtLayerBuild;
import uk.os.vt.mvt.build.MvtLayerParams;
import uk.os.vt.mvt.build.MvtLayerProps;

/**
 * Convenience class allows easy encoding of a {@link JtsMvt} to bytes.
 */
public final class MvtEncoder {

  /**
   * Encode a {@link JtsMvt} to byte[] ready for writing to a file.
   * <p>Uses {@link MvtLayerParams#DEFAULT} and {@link UserDataKeyValueMapConverter} to transform
   * the JtsMvt.</p>
   *
   * @param mvt input to encode to bytes
   * @return bytes ready for writing to a .mvt
   * @see #encode(JtsMvt, MvtLayerParams, IUserDataConverter)
   */
  public static byte[] encode(JtsMvt mvt) {
    return encode(mvt, MvtLayerParams.DEFAULT, new UserDataKeyValueMapConverter());
  }

  /**
   * Encode a {@link JtsMvt} to byte[] ready for writing to a file.
   *
   * @param mvt               input to encode to bytes
   * @param mvtLayerParams    tile creation parameters
   * @param userDataConverter converts {@link Geometry#userData} to MVT feature tags
   * @return bytes ready for writing to a .mvt
   */
  public static byte[] encode(JtsMvt mvt, MvtLayerParams mvtLayerParams,
                              IUserDataConverter userDataConverter) {

    // Build MVT
    final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

    for (JtsLayer layer : mvt.getLayers()) {
      final Collection<Geometry> layerGeoms = layer.getGeometries();

      // Create MVT layer
      final VectorTile.Tile.Layer.Builder layerBuilder =
          MvtLayerBuild.newLayerBuilder(layer.getName(), mvtLayerParams);
      final MvtLayerProps layerProps = new MvtLayerProps();

      // MVT tile geometry to MVT features
      final List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(
          layerGeoms, layerProps, userDataConverter);
      layerBuilder.addAllFeatures(features);
      MvtLayerBuild.writeProps(layerBuilder, layerProps);

      // Build MVT layer
      final VectorTile.Tile.Layer vtl = layerBuilder.build();
      tileBuilder.addLayers(vtl);
    }

    // Build MVT
    return tileBuilder.build().toByteArray();
  }
}
