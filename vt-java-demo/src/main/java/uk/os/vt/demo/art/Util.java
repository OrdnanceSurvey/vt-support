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

package uk.os.vt.demo.art;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

  private Util() {}

  /**
   * Convert extremely simple SVG pathData to a coordinate array.
   * @param pathData very simple path data
   * @param col the column offset for path data
   * @param row the row offset for path data
   * @param magify the multiple for pathdata to be increased
   * @return a list of coordinates to represent the SVG path
   */
  public static Coordinate[] toCoordinate(String pathData, int col, int row, int magify) {
    final List<Coordinate> cs = new ArrayList<Coordinate>();
    pathData = pathData.toUpperCase();
    pathData = pathData.replaceAll("C", "L");
    pathData = pathData.replaceAll("Q", "L");
    pathData = pathData.replaceAll("A", "L");

    final Matcher matchPathCmd =
        Pattern.compile("([MmLlHhVvAaQqTtCcSsZz])|([-+]?((\\d*\\.\\d+)|(\\d+))([eE][-+]?\\d+)?)")
            .matcher(pathData);

    // Tokenize
    final LinkedList<String> tokens = new LinkedList<String>();
    while (matchPathCmd.find()) {
      tokens.addLast(matchPathCmd.group());
    }

    char curCmd = 'Z';
    while (tokens.size() != 0) {
      final String curToken = tokens.removeFirst();
      final char initChar = curToken.charAt(0);
      if ((initChar >= 'A' && initChar <= 'Z') || (initChar >= 'a' && initChar <= 'z')) {
        curCmd = initChar;
      } else {
        tokens.addFirst(curToken);
      }

      switch (curCmd) {
        case 'M':
          col += nextFloat(tokens) * magify;
          row += nextFloat(tokens) * -magify;
          curCmd = 'L';
          break;
        case 'L':
          final float x1 = nextFloat(tokens) * magify;
          final float y1 = nextFloat(tokens) * -magify;
          col += x1;
          row += y1;
          cs.add(new Coordinate(col, row));
          break;
        case 'Z':
          cs.add(cs.get(0));
          break;
        default:
          throw new RuntimeException("Invalid path element");
      }
    }
    return cs.toArray(new Coordinate[cs.size()]);
  }

  private static float nextFloat(LinkedList<String> list) {
    final String s = list.removeFirst();
    return Float.parseFloat(s);
  }

}
