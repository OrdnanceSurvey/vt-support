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

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import rx.functions.Func1;
import uk.os.vt.Metadata;
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
    tileLayer.addPoint(beijing(), beijingAttributes());
    tileLayer.addPoint(sydney(), sydneyAttributes());
    tileLayer.addPoint(rio());
    tileLayer.addPoint(cairo());

    Integer total = tileLayer
            .getGeometry(5)
            .filter(filterGeometriesWithEmptyAttributes())
            .count()
            .toBlocking()
            .first();
    System.out.println("Total geometries with attributes: " + total);

    storage.getMetadata().map(metadata ->
      new Metadata.Builder(metadata).addLayer(new Metadata.Layer.Builder()
              .addField("name", "String")
              .addField("population", "Number").build()).build())
            .subscribe(System.out::println);
  }

  private static Func1<Geometry, Boolean> filterGeometriesWithEmptyAttributes() {
    return geometry -> geometry.getUserData() != null
            && geometry.getUserData() instanceof Map
            && !((Map) geometry.getUserData()).isEmpty();
  }

  private static double[] beijing() {
    return new double[] {39.918751, 116.396965};
  }

  private static Map<String, Object> beijingAttributes() {
    return new AttributeBuilder()
            .add("name", "Beijing")
            .add("population", 22063000)
            .build();
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

  private static Map<String, Object> sydneyAttributes() {
    return new AttributeBuilder()
            .add("name", "Sydney")
            .add("population", 5029768)
            .build();
  }

  public static class AttributeBuilder {

    private LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();

    public AttributeBuilder add(String key, Object value) {
      attributes.put(key, value);
      return this;
    }

    public HashMap<String, Object> build() {
      return attributes;
    }
  }
}
