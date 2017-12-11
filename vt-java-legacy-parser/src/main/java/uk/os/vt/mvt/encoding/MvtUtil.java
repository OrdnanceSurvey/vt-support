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

package uk.os.vt.mvt.encoding;

import uk.os.vt.mvt.VectorTile;

/**
 * <p>Useful misc operations for encoding 'Mapbox Vector Tiles'.</p>
 * <p>See: <a href="https://github.com/mapbox/vector-tile-spec">https://github.com/mapbox/vector-tile-spec</a></p>
 */
public final class MvtUtil {

  /**
   * Return whether the MVT geometry type should be closed with a {@link GeomCmd#ClosePath}.
   *
   * @param geomType the type of MVT geometry
   * @return true if the geometry should be closed, false if it should not be closed
   */
  public static boolean shouldClosePath(VectorTile.Tile.GeomType geomType) {
    final boolean closeReq;

    switch (geomType) {
      case POLYGON:
        closeReq = true;
        break;
      default:
        closeReq = false;
        break;
    }

    return closeReq;
  }
}
