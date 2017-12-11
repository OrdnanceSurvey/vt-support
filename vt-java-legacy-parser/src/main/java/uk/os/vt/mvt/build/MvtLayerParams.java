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


/**
 * Immutable parameters collection for Mapbox-Vector-Tile creation.
 */
public final class MvtLayerParams {

  /**
   * Default layer parameters created using {@link #MvtLayerParams()}.
   */
  public static final MvtLayerParams DEFAULT = new MvtLayerParams();


  /**
   * the resolution of the tile in 'pixel' dimensions.
   */
  public final int tileSize;

  /**
   * the resolution of the MVT local coordinate system.
   */
  public final int extent;

  /**
   * ratio of tile 'pixel' dimensions to tile extent dimensions.
   */
  public final float ratio;

  /**
   * Construct default layer sizing parameters for MVT creation.
   * <p>
   * Uses defaults:
   * <ul>
   * <li>{@link #tileSize} = 256</li>
   * <li>{@link #extent} = 4096</li>
   * </ul>
   * </p>
   *
   * @see #MvtLayerParams(int, int)
   */
  public MvtLayerParams() {
    this(256, 4096);
  }

  /**
   * Construct layer sizing parameters for MVT creation.
   *
   * @param tileSize the resolution of the tile in pixel coordinates, must be > 0
   * @param extent   the resolution of the MVT local coordinate system, must be > 0
   */
  public MvtLayerParams(int tileSize, int extent) {
    if (tileSize <= 0) {
      throw new IllegalArgumentException("tileSize must be > 0");
    }

    if (extent <= 0) {
      throw new IllegalArgumentException("extent must be > 0");
    }

    this.tileSize = tileSize;
    this.extent = extent;
    this.ratio = extent / (float) tileSize;
  }
}
