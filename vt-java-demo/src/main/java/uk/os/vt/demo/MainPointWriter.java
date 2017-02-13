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

package uk.os.vt.demo;

import java.io.File;
import java.io.IOException;

import uk.os.vt.TileLayer;
import uk.os.vt.mbtiles.StorageImpl;

public class MainPointWriter {

  /**
   * Write out points from a mb tile.
   *
   * @param args args
   * @throws IOException thrown on IO error
   * @throws InterruptedException thrown when interrupted
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final String filename = "PlanetPoints.mbtiles";
    final File file = new File(filename);

    if (!file.delete()) {
      System.out.println("cannot delete file");
    }

    final StorageImpl storage = new StorageImpl.Builder(filename).createIfNotExist().build();

    final TileLayer tileLayer = new TileLayer.Builder().setMinZoom(0).setMaxZoom(5)
        .setAttribution("Ordnance Survey").setStorage(storage).build();

    tileLayer.addPoint(london());
    tileLayer.addPoint(salisbury());
    tileLayer.addPoint(dublin());
    tileLayer.addPoint(sanFrancisco());
    tileLayer.addPoint(capeTown());
    tileLayer.addPoint(newYork());
    tileLayer.addPoint(beijing());
    tileLayer.addPoint(sydney());
    tileLayer.addPoint(rio());
    tileLayer.addPoint(cairo());
  }

  private static double[] beijing() {
    return new double[] {39.918751, 116.396965};
  }

  private static double[] cairo() {
    return new double[] {30.042396, 31.229702};
  }

  private static double[] capeTown() {
    return new double[] {-33.906784, 18.422095};
  }

  private static double[] dublin() {
    return new double[] {53.347320, -6.259639};
  }

  private static double[] london() {
    return new double[] {51.507964, -0.128024};
  }

  private static double[] newYork() {
    return new double[] {40.766583, -73.977401};
  }

  private static double[] rio() {
    return new double[] {-22.905156, -43.188553};
  }

  private static double[] salisbury() {
    return new double[] {51.064712, -1.796172};
  }

  private static double[] sanFrancisco() {
    return new double[] {37.808381, -122.409974};
  }

  private static double[] sydney() {
    return new double[] {-33.858545, 151.214591};
  }
}
