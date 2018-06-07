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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestVectorTileConfig {

  @Test
  public void checkGlobalTile() {
    double[] result = new Config.Builder()
        .minZoom(0)
        .maxZoom(0)
        .maxZoomMinX(0)
        .maxZoomMaxX(0)
        .maxZoomMinY(0)
        .maxZoomMaxY(0)
        .build()
        .getExtentAsLatLon();

    double expectedMaxLat = 85.05112877980659;
    double expectedMinLat = -85.05112877980659;
    double expectedMinLon = -180;
    double expectedMaxLon = 180;

    checkExtent(expectedMinLon, expectedMinLat, expectedMaxLon, expectedMaxLat, result);
  }

  @Test
  public void checkGlobalZ1() {
    double[] result = new Config.Builder()
        .minZoom(1)
        .maxZoom(1)
        .maxZoomMinX(0)
        .maxZoomMaxX(1)
        .maxZoomMinY(0)
        .maxZoomMaxY(1)
        .build()
        .getExtentAsLatLon();

    double expectedMaxLat = 85.05112877980659;
    double expectedMinLat = -85.05112877980659;
    double expectedMinLon = -180;
    double expectedMaxLon = 180;

    checkExtent(expectedMinLon, expectedMinLat, expectedMaxLon, expectedMaxLat, result);
  }

  @Test
  public void checkGlobalZ2() {
    double[] result = new Config.Builder()
        .minZoom(2)
        .maxZoom(2)
        .maxZoomMinX(0)
        .maxZoomMaxX(3)
        .maxZoomMinY(0)
        .maxZoomMaxY(3)
        .build()
        .getExtentAsLatLon();

    double expectedMaxLat = 85.05112877980659;
    double expectedMinLat = -85.05112877980659;
    double expectedMinLon = -180;
    double expectedMaxLon = 180;

    checkExtent(expectedMinLon, expectedMinLat, expectedMaxLon, expectedMaxLat, result);
  }

  @Test
  public void checkGlobalZ1Z2() {
    double[] result = new Config.Builder()
        .minZoom(1)
        .maxZoom(2)
        .maxZoomMinX(0)
        .maxZoomMaxX(3)
        .maxZoomMinY(0)
        .maxZoomMaxY(3)
        .build()
        .getExtentAsLatLon();

    double expectedMaxLat = 85.05112877980659;
    double expectedMinLat = -85.05112877980659;
    double expectedMinLon = -180;
    double expectedMaxLon = 180;

    checkExtent(expectedMinLon, expectedMinLat, expectedMaxLon, expectedMaxLat, result);
  }

  @Test
  public void checkLondonZ11() {
    double[] result = new Config.Builder()
        .minZoom(10)
        .maxZoom(11)
        .maxZoomMinX(1022)
        .maxZoomMaxX(1023)
        .maxZoomMinY(680)
        .maxZoomMaxY(681)
        .build()
        .getExtentAsLatLon();

    double expectedMinLon = -0.3515625;
    double expectedMinLat = 51.39920565355378;
    double expectedMaxLon = 0;
    double expectedMaxLat = 51.6180165487737;

    checkExtent(expectedMinLon, expectedMinLat, expectedMaxLon, expectedMaxLat, result);
  }

  @Test
  public void checkLondonZ11Wider() {
    double[] result = new Config.Builder()
        .minZoom(10)
        .maxZoom(11)
        .maxZoomMinX(1022)
        .maxZoomMaxX(1024)
        .maxZoomMinY(680)
        .maxZoomMaxY(681)
        .build()
        .getExtentAsLatLon();

    double expectedMinLon = -0.3515625;
    double expectedMinLat = 51.39920565355378;
    double expectedMaxLon = 0.17578125;
    double expectedMaxLat = 51.6180165487737;

    checkExtent(expectedMinLon, expectedMinLat, expectedMaxLon, expectedMaxLat, result);
  }

  private void checkExtent(double expectedMinLon, double expectedMinLat, double expectedMaxLon,
                           double expectedMaxLat, double[] result) {
    double delta = 0.0001;
    assertEquals(expectedMinLon, result[0], delta);
    assertEquals(expectedMinLat, result[1], delta);
    assertEquals(expectedMaxLon, result[2], delta);
    assertEquals(expectedMaxLat, result[3], delta);
  }

  private static class Config {

    public static class Builder {

      private int minZoom;
      private int maxZoom;
      private int maxZoomMinX;
      private int maxZoomMinY;
      private int maxZoomMaxX;
      private int maxZoomMaxY;

      public Builder minZoom(int minZoom) {
        this.minZoom = minZoom;
        return this;
      }

      public Builder maxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
        return this;
      }

      public Builder maxZoomMinX(int maxZoomMinX) {
        this.maxZoomMinX = maxZoomMinX;
        return this;
      }

      public Builder maxZoomMinY(int maxZoomMinY) {
        this.maxZoomMinY = maxZoomMinY;
        return this;
      }


      public Builder maxZoomMaxX(int maxZoomMaxX) {
        this.maxZoomMaxX = maxZoomMaxX;
        return this;
      }

      public Builder maxZoomMaxY(int maxZoomMaxY) {
        this.maxZoomMaxY = maxZoomMaxY;
        return this;
      }

      public VectorTileConfig build() {
        return new VectorTileConfig(minZoom, maxZoom, maxZoomMinX, maxZoomMinY,
            maxZoomMaxX, maxZoomMaxY);
      }
    }
  }
}
