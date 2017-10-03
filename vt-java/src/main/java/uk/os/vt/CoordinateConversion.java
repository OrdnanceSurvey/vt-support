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

import com.vividsolutions.jts.geom.Point;

/**
 * Google tiling scheme with the origin at the top left, which is also the origin for a vector tile.
 */
public final class CoordinateConversion {

  private CoordinateConversion() {}

  public static int[] toTileCoordinates(Point point, int zoom) {
    return toTileCoordinates(point, zoom, 4096);
  }

  public static int[] toTileCoordinates(Point point, int zoom, double measurementSpace) {
    return toTileCoordinates(point.getY(), point.getX(), zoom, measurementSpace);
  }

  public static int[] toTileCoordinates(double[] latlon, int zoom) {
    return toTileCoordinates(latlon, zoom, 4096);
  }

  public static int[] toTileCoordinates(double[] latlon, int zoom, double measurementSpace) {
    return toTileCoordinates(latlon[0], latlon[1], zoom, measurementSpace);
  }

  /**
   * Convert a lat lon to tile coordinates.
   *
   * @param lat the lat
   * @param lon the lon
   * @param zoom the zoom level
   * @param measurementSpace the internal resolution of the vector tile, normally 4096 or 256
   * @return the zoom, row, col, x1, y1
   */
  public static int[] toTileCoordinates(double lat, double lon, int zoom, double measurementSpace) {
    // clamp values
    int maxTiles = 1 << zoom;
    int minTileIndex = 0;
    int maxTileIndex = maxTiles - 1;

    // Infinite grid
    final double xtile = (lon + 180) / 360 * (1 << zoom);
    final double ytile =
        (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI)
            / 2 * (1 << zoom);

    int row = Math.min(maxTileIndex, Math.max(minTileIndex, (int) Math.floor(xtile)));
    int col = Math.min(maxTileIndex, Math.max(minTileIndex, (int) Math.floor(ytile)));
    int x1 = (int) ((xtile - row) * measurementSpace);
    int y1 = (int) ((ytile - col) * measurementSpace);

    return new int[] {zoom, row, col, x1, y1};
  }

  /**
   * The point coordinates are ultimately in respect to the tile coordinates. Note: tile coordinates
   * are assumed to be in the Google tiling scheme
   *
   * @param zoom zoom level
   * @param row row tile coordinate
   * @param col column tile coordinate
   * @param point the point
   * @param measurementSpace the internal resolution of the vector tile, normally 4096 or 256
   * @return double[] the lat and lon
   */
  public static double[] toLatLon(int zoom, int row, int col, Point point,
      double measurementSpace) {
    return toLatLon(zoom, row, col, (int) point.getX(), (int) point.getY(), measurementSpace);
  }

  public static double[] toLatLon(int[] coords) {
    return toLatLon(coords, 4096);
  }

  public static double[] toLatLon(int[] coords, double measurementSpace) {
    return toLatLon(coords[0], coords[1], coords[2], coords[3], coords[4], measurementSpace);
  }

  /**
   * Convert internal tile position to Lat Lon.
   *
   * @param zoom the zoom level
   * @param col the column
   * @param row the row
   * @param x1 the x1 coord
   * @param y1 the y1 coord
   * @param measurementSpace the internal resolution of the vector tile, normally 4096 or 256
   * @return the lat lon coordinates
   */
  public static double[] toLatLon(int zoom, int col, int row, int x1, int y1,
      double measurementSpace) {
    final double xTileCoordinate = (1 / measurementSpace) * x1;
    final double yTileCoordinate = 1 - (1 / measurementSpace * y1);

    final double xf = col + xTileCoordinate;
    final double yf = (row + 1) - yTileCoordinate;

    final double lat = tile2lat(yf, zoom);
    final double lon = tile2lon(xf, zoom);

    return new double[] {lat, lon};
  }

  public static double[] toLatLon(int zoom, int col, int row, Point point) {
    return toLatLon(zoom, col, row, point, 4096F);
  }

  public static double tile2lat(double row, int zoom) {
    final double n = Math.PI - (2.0 * Math.PI * row) / Math.pow(2.0, zoom);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }

  public static double tile2lat(int row, int zoom) {
    final double n = Math.PI - (2.0 * Math.PI * row) / Math.pow(2.0, zoom);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }

  // x is normally an int _but_ within the tile is a fraction
  public static double tile2lon(double col, int zoom) {
    return col / Math.pow(2.0, zoom) * 360.0 - 180;
  }

  public static double tile2lon(int col, int zoom) {
    return col / Math.pow(2.0, zoom) * 360.0 - 180;
  }

  /**
   * Convert a TMS to Google Tile coordinates.
   *
   * @param zoom level for the TMS to Google coordinate conversion
   * @param col the X TMS coordinate
   * @param row the Y TMS coordinate
   * @return zxy array with Google coordinates
   */
  public static int[] fromTms(int zoom, int col, int row) {
    return new int[] {zoom, col, flipY(row, zoom)};
  }

  public static int[] toTms(int zoom, int col, int row) {
    // same function but named for clarity
    return fromTms(zoom, col, row);
  }

  private static int flipY(int row, int zoom) {
    return (int) (Math.pow(2, zoom) - row - 1);
  }
}
