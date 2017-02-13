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

package uk.os.vt.demo.art;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IGeometryFilter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TileGeomResult;
import com.wdtinc.mapbox_vector_tile.adapt.jts.UserDataIgnoreConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Picture {

  // Default MVT parameters
  private static final MvtLayerParams DEFAULT_MVT_PARAMS = new MvtLayerParams();

  // Do not filter tile geometry
  private static final IGeometryFilter ACCEPT_ALL_FILTER = geometry -> true;

  public static class Builder {
    private String activeLayer = "default";
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final Map<String, List<Geometry>> layers = new HashMap<>();

    public Builder() {}

    /**
     * Set the layer for graphics to be added.
     * @param layerName to set as active
     * @return this builder
     */
    public Picture.Builder setAcetate(String layerName) {
      activeLayer = layerName;
      return this;
    }

    /**
     * Set the layer for graphics to be added.
     * @return the active layer (acetate)
     */
    private List<Geometry> getActiveLayer() {
      final boolean isDefined = layers.containsKey(activeLayer);
      if (!isDefined) {
        layers.put(activeLayer, new ArrayList<>());
      }
      return layers.get(activeLayer);
    }

    /**
     * Add polygon coordinates.
     * @param coordinates polygon coordinates to add to current layer
     * @return this builder
     */
    public Builder poly(Coordinate[]... coordinates) {
      for (Coordinate[] c : coordinates) {
        poly(c);
      }
      return this;
    }

    /**
     * Add polygon coordinates.
     * @param coordinates polygon coordinates to add to current layer
     * @return this builder
     */
    public Picture.Builder poly(Coordinate[] coordinates) {
      getActiveLayer().add(geometryFactory.createPolygon(coordinates));
      return this;
    }

    /**
     * Add linestring coordinates.
     * @param coordinates linestring coordinates to add to current layer
     * @return this builder
     */
    public Builder linestring(Coordinate[]... coordinates) {
      for (Coordinate[] c : coordinates) {
        linestring(c);
      }
      return this;
    }

    /**
     * Add linestring coordinates.
     * @param coordinates linestring coordinates to add to current layer
     * @return this builder
     */
    public Builder linestring(Coordinate[] coordinates) {
      getActiveLayer().add(geometryFactory.createLineString(coordinates));
      return this;
    }

    /**
     * Add geometry coordinates.
     * @param geom geometry to add to current layer
     * @return this builder
     */
    public Picture.Builder add(Geometry geom) {
      getActiveLayer().add(geom);
      return this;
    }

    /**
     * Build the picture.
     * @return the built picture as vector tile bytes
     */
    public byte[] build() {
      final Envelope tileEnvelope = new Envelope(0d, 4096d, 0d, 4096d);

      // Build MVT
      final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

      for (Map.Entry<String, List<Geometry>> layer : layers.entrySet()) {
        String layerName = layer.getKey();
        final List<Geometry> layerGeoms = layer.getValue();

        final TileGeomResult tileGeom = JtsAdapter.createTileGeom(layerGeoms, tileEnvelope,
            geometryFactory, DEFAULT_MVT_PARAMS, ACCEPT_ALL_FILTER);

        // Create MVT layer
        final VectorTile.Tile.Layer.Builder layerBuilder =
            MvtLayerBuild.newLayerBuilder(layerName, DEFAULT_MVT_PARAMS);
        final MvtLayerProps layerProps = new MvtLayerProps();
        final UserDataIgnoreConverter ignoreUserData = new UserDataIgnoreConverter();

        // MVT tile geometry to MVT features
        final List<VectorTile.Tile.Feature> features =
            JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, ignoreUserData);
        layerBuilder.addAllFeatures(features);
        MvtLayerBuild.writeProps(layerBuilder, layerProps);

        // Build MVT layer
        final VectorTile.Tile.Layer vtLayer = layerBuilder.build();
        tileBuilder.addLayers(vtLayer);
      }
      /// Build MVT
      return tileBuilder.build().toByteArray();
    }
  }
}
