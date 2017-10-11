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
import uk.os.vt.Storage;
import uk.os.vt.demo.geo.CityPoints;
import uk.os.vt.demo.util.PrintUtil;
import uk.os.vt.fluent.DataSource;

public class MainPointWriter {

  private static final String FILE_NAME = "demo_cities.mbtiles";

  /**
   * Create a demonstration vector tile mbtiles file for cities around the globe.
   *
   * @param args ignored
   * @throws IOException thrown if problem with storage
   */
  public static void main(String[] args) throws IOException {

    File storageFile = new File(FILE_NAME);
    if (storageFile.exists() && !storageFile.delete()) {
      System.err.println("Problem deleting storage file");
      return;
    }

    System.out.println("Creating vector tiles with a 'cities' layer");

    Storage storage = new uk.os.vt.mbtiles.StorageImpl.Builder(FILE_NAME)
        .createIfNotExist().build();

    // Let's create a cities layer
    new DataSource(storage, Schemas.CitySchemaV1.get())
        .using("cities")
        .add(CityPoints.london())
        .add(CityPoints.salisbury())
        .add(CityPoints.dublin())
        .add(CityPoints.sanFrancisco())
        .add(CityPoints.capeTown())
        .add(CityPoints.newYork())
        .add(CityPoints.beijing())
        .add(CityPoints.sydney())
        .add(CityPoints.rio())
        .add(CityPoints.cairo())
        .commit();

    // Log the contents tile covering UK
    log(storage, 6, 31, 21);

    System.out.println("Done!  Created vector tiles (cities)");
  }

  private static void log(Storage storage, int zoom, int col, int row) throws IOException {
    PrintUtil.printEntry(storage.getEntry(zoom, col, row).blockingFirst());
  }
}
