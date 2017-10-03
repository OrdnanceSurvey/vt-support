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

/**
 * The tile key.
 *
 * <p>Encapsulate ZXY / zoom, column, row values.
 */
public class Key {

  private static final int MAX_ZOOM_LEVEL = 22;
  private static final int MIN_ZOOM_LEVEL = 0;

  private final int zoomLevel;
  private final int column;
  private final int row;

  /**
   * Define the key to uniquely reference a vector tile.
   *
   * @param zoomLevel to specify the level of the quad tree
   * @param column the x axis
   * @param row the y axis
   */
  public Key(int zoomLevel, int column, int row) {
    validate(zoomLevel, column, row);
    this.zoomLevel = zoomLevel;
    this.column = column;
    this.row = row;
  }

  public int getZ() {
    return zoomLevel;
  }

  public int getX() {
    return column;
  }

  public int getY() {
    return row;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Key key = (Key) obj;

    if (zoomLevel != key.zoomLevel) {
      return false;
    }
    if (column != key.column) {
      return false;
    }
    return row == key.row;
  }

  @Override
  public int hashCode() {
    int result = zoomLevel;
    result = 31 * result + column;
    result = 31 * result + row;
    return result;
  }

  @Override
  public String toString() {
    return "Key{"
        + "zoomLevel=" + zoomLevel
        + ", column=" + column
        + ", row=" + row
        + '}';
  }

  private static void validate(int zoomLevel, int column, int row) {
    final boolean isValidZoomLevel = MIN_ZOOM_LEVEL <= zoomLevel && zoomLevel <= MAX_ZOOM_LEVEL;
    if (!isValidZoomLevel) {
      throw new IllegalArgumentException(String.format("invalid tile zoom level %d", zoomLevel));
    }

    final double tilesOnAxisForZoom = Math.pow(2, zoomLevel);

    final boolean isValidCoordinates =
        0 <= column && column < tilesOnAxisForZoom && 0 <= row && row < tilesOnAxisForZoom;
    if (!isValidCoordinates) {
      final String message =
          String.format("invalid tile coordinate: %d %d %d (z x y)", zoomLevel, column, row);
      throw new IllegalArgumentException(message);
    }
  }
}
