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

package uk.os.vt.demo.art.nature;

import static uk.os.vt.demo.art.Util.toCoordinate;

import com.vividsolutions.jts.geom.Coordinate;

public class Shapes {

  private Shapes() {}

  /**
   * Bubble picture.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier for image coordinates
   * @return the coordinates to represent a bubble
   */
  public static Coordinate[] bubble(int col, int row, int scale) {
    final String pathData = "M3.929,4.361l-0.016,1.434l-1.653,0.458l0.051,-1.598l1.618,-0.294Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Bubble 2 picture.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier for image coordinates
   * @return the coordinates to represent a bubble
   */
  public static Coordinate[] bubble2(int col, int row, int scale) {
    final String pathData = "M4.929,1.361l-0.016,1.434l-1.653,0.458l0.051,-1.598l1.618,-0.294Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Fish picture.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier for image coordinates
   * @return the coordinates to represent a fish
   */
  public static Coordinate[] fish(int col, int row, int scale) {
    final String pathData =
        "M28.929,7l-0.085,21.222l-8.875,-9.528l-6.312,6.273l-4.577,1.849l-5.561,-2.282l-2.017,"
            + "-3.869l4.504,-2.506l-5.341,-3.089l1.264,-5.244l2.847,-2.669l5.032,-2.796l4.413,"
            + "1.823l2.208,2.361l3.813,6.455l8.687,-8Z";
    return toCoordinate(pathData, col, row, scale);
  }
}
