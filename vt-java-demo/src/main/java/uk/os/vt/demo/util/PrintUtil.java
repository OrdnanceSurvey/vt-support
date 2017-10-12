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

package uk.os.vt.demo.util;

import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import uk.os.vt.Entry;
import uk.os.vt.parsers.VtParser;
import uk.os.vt.parsers.VtParserWdtinc;

public class PrintUtil {

  private static final VtParser DEFAULT_PARSER = new VtParserWdtinc();

  /**
   * Print a tile entry.
   *
   * @param entry the tile entry
   * @throws IOException thrown when reading error
   */
  public static void printEntry(Entry entry) throws IOException {
    System.out.println(String.format("%n%nEntry: %d/%d/%d", entry.getZoomLevel(),
        entry.getColumn(), entry.getRow()));
    System.out.println("---------------");

    printGeoms(DEFAULT_PARSER.parse(entry));
  }

  /**
   * Print a tile entry.
   *
   * @param entry the tile entry
   * @param parser the tile parser
   * @throws IOException thrown when reading error
   */
  public static void printEntry(Entry entry, VtParser parser) throws IOException {
    System.out.println(String.format("%n%nEntry: %d/%d/%d", entry.getZoomLevel(),
        entry.getColumn(), entry.getRow()));
    System.out.println("---------------");

    printGeoms(parser.parse(entry));
  }

  /**
   * Print geometries.
   *
   * @param geoms the geometries
   * @throws IOException thrown when IO error
   */
  public static void printGeoms(Collection<Geometry> geoms) throws IOException {
    System.out.println("Attribute Info");
    System.out.println("---------------");

    for (Geometry geom : geoms) {
      if (geom.getUserData() == null) {
        System.out.println("no user data");
        return;
      }

      Map<?,?> attributes = (Map) geom.getUserData();
      for (Map.Entry entry : attributes.entrySet()) {
        System.out.println(entry.getKey() + " -> " + entry.getValue());
      }
      System.out.println("");
    }
  }
}
