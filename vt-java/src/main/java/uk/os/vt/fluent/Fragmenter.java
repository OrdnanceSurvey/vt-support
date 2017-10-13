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

import static uk.os.vt.fluent.Fragmenter.SphericalMercator.toMercator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IGeometryFilter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TileGeomResult;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.os.vt.CoordinateConversion;
import uk.os.vt.Key;

class Fragmenter {

  // Vector Tile Grid
  private static final int VT_MEASUREMENT_SPACE = 4096;
  private static final int VT_MIN = 0;
  private static final int VT_MAX = VT_MIN + VT_MEASUREMENT_SPACE;

  /**
   * Default MVT parameters.
   */
  private static final MvtLayerParams DEFAULT_MVT_PARAMS = new MvtLayerParams();

  /**
   * Do not filter tile geometry.
   */
  private static final IGeometryFilter ACCEPT_ALL_FILTER = geometry -> true;

  /**
   * Fragment geometry in all given layers.
   *
   * @param layers containing geometry that needs to be fragmented in the Google Tiling Scheme
   */
  static Map<Key, JtsMvt> fragment(Set<FluentLayer> layers) {
    Map<Key, JtsMvt> bucket = new HashMap<>();

    for (FluentLayer layer : layers) {
      Collection<Geometry> geometries = layer.getGeometries();

      int minZoom = layer.getMetadata().getMinZoom();
      int maxZoom = layer.getMetadata().getMaxZoom();

      for (Geometry geometry : geometries) {
        fragment(geometry, layer.getName(), minZoom, maxZoom, bucket);
      }
    }
    return bucket;
  }

  private static void fragment(Geometry geometry, String layerName, int min, int max,
                               Map<Key, JtsMvt> bucket) {
    for (int z = min; z <= max; z++) {

      int[] tilingSchemeBoundingBox = getTilingSchemeBoundingBox(z, geometry);

      int minx = tilingSchemeBoundingBox[0];
      int miny = tilingSchemeBoundingBox[1];
      int maxx = tilingSchemeBoundingBox[2];
      int maxy = tilingSchemeBoundingBox[3];

      // these are the cells of interest (ones that the geometry may cover)
      for (int x = minx; x <= maxx; x++) {
        for (int y = miny; y <= maxy; y++) {
          addToStaging(z, x, y, layerName, geometry, bucket);
        }
      }
    }
  }

  /**
   * Add the given geometry to the given tile as defined by the z, x, y values.
   *
   * @param z the tile zoom coordinate value
   * @param x the tile column coordinate value
   * @param y the tile row coordinate value
   * @param layerName name of the layer for the geometry
   * @param geometry  WGS84 geometry
   */
  private static void addToStaging(int z, int x, int y, String layerName, Geometry geometry,
                                   Map<Key, JtsMvt> bucket) {
    double[] ul = CoordinateConversion.toLatLon(z, x, y, VT_MIN, VT_MIN, VT_MEASUREMENT_SPACE);
    double[] lr = CoordinateConversion.toLatLon(z, x, y, VT_MAX, VT_MAX, VT_MEASUREMENT_SPACE);

    Envelope tileEnvelope = new Envelope(ul[1], lr[1], ul[0], lr[0]);

    final GeometryFactory geomFactory = new GeometryFactory();

    // Build MVT tile geometry
    // TODO REMOVE HACK
    final TileGeomResult tileGeom = JtsAdapter.createTileGeom(toMercator(geometry),
        toMercator(tileEnvelope), geomFactory, DEFAULT_MVT_PARAMS, ACCEPT_ALL_FILTER);

    if (tileGeom.mvtGeoms.isEmpty()) {
      // System.out.println(String.format("Empty: %d %d %d", z, x, y));
      return;
    }

    // ########################################################
    // # Update TEMP
    // ########################################################
    Key key = new Key(z, x, y);
    if (!bucket.containsKey(key)) {
      bucket.put(key, new JtsMvt(new JtsLayer(layerName)));
    } else if (bucket.get(key).getLayer(layerName) == null) {
      // TODO consider PR
      bucket.get(key).getLayersByName().put(layerName, new JtsLayer(layerName));
      //bucket.get(key).addLayers(new JtsLayer(layerName));
    }

    JtsMvt mvt = bucket.get(key);
    JtsLayer layer = mvt.getLayer(layerName);
    layer.getGeometries().addAll(tileGeom.mvtGeoms); // TODO verify this is ok!!!!
    // ########################################################
    // # EO Update TEMP
    // ########################################################
  }

  private static int[] getTilingSchemeBoundingBox(int z, Geometry geometry) {
    Geometry geometryEnvelope = geometry.getEnvelope();
    if (geometryEnvelope instanceof Polygon) {
      Polygon polygon = (Polygon) geometryEnvelope;
      Coordinate[] coordinates = polygon.getCoordinates();

      int minx = Integer.MAX_VALUE;
      int maxx = Integer.MIN_VALUE;
      int miny = Integer.MAX_VALUE;
      int maxy = Integer.MIN_VALUE;

      for (int i = 0; i < coordinates.length - 1; i++) {
        Coordinate coordinate = coordinates[i];
        int[] tilecoords = CoordinateConversion.toTileCoordinates(new double[]{
            coordinate.y, coordinate.x}, z);

        minx = Math.min(minx, tilecoords[1]);
        maxx = Math.max(maxx, tilecoords[1]);
        miny = Math.min(miny, tilecoords[2]);
        maxy = Math.max(maxy, tilecoords[2]);
      }
      return new int[]{minx, miny, maxx, maxy};
    } else if (geometryEnvelope instanceof Point) {
      // TODO design out
      Point point = (Point) geometryEnvelope;
      int[] tilecoords = CoordinateConversion.toTileCoordinates(new double[]{
          point.getCoordinate().y, point.getCoordinate().x}, z);
      return new int[]{tilecoords[1], tilecoords[2], tilecoords[1], tilecoords[2]};
    }
    throw new IllegalArgumentException("unsupported geometry type");
  }

  public static class SphericalMercator {
    public static final double RADIUS = 6378137.0; /* in meters on the equator */

    public static Envelope toMercator(Envelope envelope) {
      return new Envelope(lon2x(envelope.getMinX()),
          lon2x(envelope.getMaxX()),
          lat2y(envelope.getMinY()),
          lat2y(envelope.getMaxY()));
    }

    public static Geometry toMercator(Geometry geometry) {
      Geometry clone = (Geometry) geometry.clone();
      clone.apply(new CoordinateFilter() {
        @Override
        public void filter(Coordinate coord) {
          coord.setCoordinate(new Coordinate(lon2x(coord.x), lat2y(coord.y), coord.z));
        }
      });
      clone.geometryChanged();
      return clone;
    }

    /* These functions take their length parameter in meters and return an angle in degrees */
    public static double y2lat(double value) {
      return Math.toDegrees(Math.atan(Math.exp(value / RADIUS)) * 2 - Math.PI / 2);
    }

    public static double x2lon(double value) {
      return Math.toDegrees(value / RADIUS);
    }

    /* These functions take their angle parameter in degrees and return a length in meters */
    public static double lat2y(double value) {
      return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(value) / 2)) * RADIUS;
    }

    public static double lon2x(double value) {
      return Math.toRadians(value) * RADIUS;
    }
  }
}
