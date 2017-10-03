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
import uk.os.vt.demo.geo.Boxes;
import uk.os.vt.demo.geo.Countries;
import uk.os.vt.demo.geo.Outbreaks;
import uk.os.vt.demo.geo.Points;
import uk.os.vt.fluent.DataSource;

/**
 * Demonstration of fluent interface to create and manage a "disease outbreak" vector data source.
 */
public class FluentMain {

  private static final String FILE_NAME = "demo_outbreaks.mbtiles";

  /**
   * Create a demonstration vector tile mbtiles file.
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

    System.out.println("Creating vector tiles with 'countries', 'outbreaks' and 'boxes' layers");

    Storage storage = new uk.os.vt.mbtiles.StorageImpl.Builder(FILE_NAME)
        .createIfNotExist().build();

    // Let's create a countries layer
    new DataSource(storage, Schemas.OutbreakSchemaV1.get())
        .using("countries")
        .add(Countries.australia())
        .add(Countries.bermuda())
        .using("outbreaks")
        .add(Outbreaks.congo())
        // not in schema and will generate warning
        .using("boxes")
        .add(Boxes.createLowerLeftExtremeZ4())
        .add(Boxes.createLowerRightExtremeZ4())
        .add(Boxes.createUpperLeftExtremeZ4())
        .add(Boxes.createUpperRightExtremeZ4())
        .commit();

    // Let's open up the same storage and append some US geography
    new DataSource(storage, Schemas.OutbreakSchemaV1.get())
        .using("countries")
        .add(Countries.unitedStatesOfAmerica())
        .using("outbreaks")
        .add(Outbreaks.unitedStatesOfAmerica())
        .using("countries")
        .add(Points.unitedKingdom())
        .commit();

    System.out.println("Done!  Created vector tiles (countries, outbreaks and boxes)");
  }
}
