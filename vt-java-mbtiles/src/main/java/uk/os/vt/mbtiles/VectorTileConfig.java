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

package uk.os.vt.mbtiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VectorTileConfig {

  private static final Logger LOG = LoggerFactory.getLogger(VectorTileConfig.class.getSimpleName());

  private final int maxZoom;
  private final int minZoom;
  private final int maxZoomMinX;
  private final int maxZoomMinY;
  private final int maxZoomMaxX;
  private final int maxZoomMaxY;

  public VectorTileConfig(int minZoom, int maxZoom, int maxZoomMinX, int maxZoomMinY,
                          int maxZoomMaxX, int maxZoomMaxY) {
    this.minZoom = minZoom;
    this.maxZoom = maxZoom;
    this.maxZoomMaxX = maxZoomMaxX;
    this.maxZoomMaxY = maxZoomMaxY;
    this.maxZoomMinX = maxZoomMinX;
    this.maxZoomMinY = maxZoomMinY;
  }

  double[] getExtentAsLatLon() {
    // TODO verify
    LOG.info("WARNING - check this!!!"); // TODO
    return new double[] {tile2lat(maxZoomMinY, maxZoom), tile2lat(maxZoomMaxY + 1, maxZoom),
        tile2lon(maxZoomMinX, maxZoom), tile2lon(maxZoomMaxX + 1, maxZoom)};
  }

  int getMaxZoom() {
    return maxZoom;
  }

  int getMinZoom() {
    return minZoom;
  }

  double tile2lon(int col, int zoom) {
    return col / Math.pow(2.0, zoom) * 360.0 - 180;
  }

  double tile2lat(int row, int zoom) {
    final double n = Math.PI - (2.0 * Math.PI * row) / Math.pow(2.0, zoom);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }
}
