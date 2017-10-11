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

import static uk.os.vt.demo.util.RandomUtil.getRandom;
import static uk.os.vt.demo.util.ResourceUtil.getFile;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.os.vt.Storage;
import uk.os.vt.demo.util.PrintUtil;
import uk.os.vt.demo.util.StorageUtil;
import uk.os.vt.parsers.VtParser;
import uk.os.vt.parsers.VtParserElectronicChartCentre;
import uk.os.vt.parsers.VtParserWdtinc;

public class Main {

  private static final String SINGLE_ZOOM_LEVEL_MBTILE =
      "Boundary-line-historic-counties_regionz5.mbtiles";
  private static final String SINGLE_ZOOM_LEVEL_FILESYSTEM = "windspeed";
  private static final String SINGLE_ZOOM_LEVEL_FILESYSTEM_DAMAGED = "windspeed_damaged";

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private Main() {}

  /**
   * Main demo.
   *
   * @param args the args
   * @throws IOException thrown on IO error
   */
  public static void main(String[] args) throws IOException {

    // Demo files
    final File file = getFile(getRandom(SINGLE_ZOOM_LEVEL_FILESYSTEM,
        SINGLE_ZOOM_LEVEL_FILESYSTEM_DAMAGED, SINGLE_ZOOM_LEVEL_MBTILE));
    System.out.println("The lucky data is: " + file.getAbsolutePath());

    // Pick parser at random
    final VtParser parser = getRandom(new VtParserElectronicChartCentre(), new VtParserWdtinc());
    System.out.println("The lucky parser is: " + parser.getClass());

    // Print the parse geometries
    final Storage storage = StorageUtil.getStorage(file);
    storage.getEntries().blockingForEach(entry -> {
      try {
        PrintUtil.printEntry(entry, parser);
      } catch (final IOException ex) {
        final String problemCoordinates = String.format("Location: %d/%d/%d", entry.getZoomLevel(),
            entry.getColumn(), entry.getRow());

        LOG.error("problem getting geometries. " + problemCoordinates, ex);
      }
    });
  }
}
