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

import java.util.Arrays;

public class Entry {

  private static final int MAX_ZOOM_LEVEL = 22;
  private static final int MIN_ZOOM_LEVEL = 0;

  private static final long KILOBYTE = 1024;
  private static final long MAX_VECTOR_TILE_SIZE = KILOBYTE * 500;

  private final int zoomLevel;
  private final int column;
  private final int row;
  private final byte[] vector;

  /**
   * @param zoomLevel The zoom level
   * @param column The tile in x direction
   * @param row The tile in y direction
   * @param vector uncompressed vector tile bytes - no gzip compression etc.
   */
  public Entry(int zoomLevel, int column, int row, byte[] vector) {
    validate(zoomLevel, column, row, vector);
    this.zoomLevel = zoomLevel;
    this.column = column;
    this.row = row;
    // shallow copy (else consider serialization and deserialization)
    this.vector = vector.clone();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    final Entry entry = (Entry) other;

    if (zoomLevel != entry.zoomLevel) {
      return false;
    }
    if (column != entry.column) {
      return false;
    }
    if (row != entry.row) {
      return false;
    }
    return Arrays.equals(vector, entry.vector);
  }

  @Override
  public int hashCode() {
    int result = zoomLevel;
    result = 31 * result + column;
    result = 31 * result + row;
    result = 31 * result + Arrays.hashCode(vector);
    return result;
  }

  public final int getZoomLevel() {
    return zoomLevel;
  }

  public final int getColumn() {
    return column;
  }

  public final int getRow() {
    return row;
  }

  /**
   * Get the uncompressed version of vector data.
   *
   * @return uncompressed version of vector data
   */
  public final byte[] getVector() {
    // shallow copy (else consider serialization and deserialization)
    return vector.clone();
  }

  @Override
  public String toString() {
    return "Entry{" + "zoomLevel=" + zoomLevel + ", column=" + column + ", row=" + row + ", vector="
        + (vector == null ? null : Arrays.hashCode(vector)) + '}';
  }

  private static void validate(int zoomLevel, int column, int row, byte[] vector) {
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

    final boolean isValidVtSize = vector.length <= MAX_VECTOR_TILE_SIZE;
    if (!isValidVtSize) {
      throw new IllegalArgumentException("Illegal vector tile - bytes exceeds 500kb!");
    }
  }
}
