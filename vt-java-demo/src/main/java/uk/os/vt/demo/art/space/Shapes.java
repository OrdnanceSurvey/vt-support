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

package uk.os.vt.demo.art.space;

import static uk.os.vt.demo.art.Util.toCoordinate;

import org.locationtech.jts.geom.Coordinate;

/**
 * Space invaders shapes.
 */
public class Shapes {

  private Shapes(){}


  /**
   * Bomb picture.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale factor
   * @return the coordinates to represent a bomb
   */
  public static Coordinate[] bomb(int col, int row, int scale) {
    final String pathData = "M28.929,0l-14.085,9.222l11.125,13.472l-13.707,10.636l5.24,"
        + "-10.956l-13.573,-9.548l14.313,-12.826l10.687,0Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Bullet picture.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale factor
   * @return the coordinates to represent a bullet
   */
  public static Coordinate[] bullet(int col, int row, int scale) {
    final String pathData =
        "M16.361,0c0.46,14.938 -0.623,31.281 -0.623,31.281l-5.738,-0.499l0.146,-30.782l6.215,0Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Enemy picture.
   * @param col the x to offset
   * @param row the y to offset
   * @return the coordinates to represent an 'enemy'
   */
  public static Coordinate[] enemy(int col, int row) {
    return enemy(col, row, 10);
  }

  private static Coordinate[] enemy(int col, int row, int scale) {
    final String pathData =
        "M22.694,30.208l2.671,-5.18l-4.499,-1.483l-4.573,1.634l-5.035,-2.017l-3.136,1.008l0.844,"
            + "5.956l-2.586,2.49l-6.38,-6.316c0,0 0.708,-1.956 1.574,-2.562l1.573,-1.102l-1.573,"
            + "-1.467l-1.574,-4.387l8.146,-9.645l8.215,-7.137l10.394,7.665l6.102,8.988l-1.567,"
            + "4.325l-1.566,1.667l3.133,3.098l-3.198,4.639l-3.521,2.948l-3.444,-3.122Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Heart picture.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale factor
   * @return the coordinates to represent a heart
   */
  public static Coordinate[] heart(int col, int row, int scale) {
    final String pathData =
        "M12 21.35l-1.45-1.32c-5.15-4.67-8.55-7.75-8.55-11.53 0-3.08 2.42-5.5 5.5-5.5"
            + " 1.74 0 3.41.81 4.5 2.09 1.09-1.28 2.76-2.09 4.5-2.09 3.08 0 5.5 2.42 5.5 5.5 0 "
            + "3.78-3.4 6.86-8.55 11.54l-1.45 1.31z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Shooter picture.
   * @param col the x to offset
   * @param row the y to offset
   * @return the coordinates to represent a shooter
   */
  public static Coordinate[] shooter(int col, int row) {
    return shooter(col, row, 10);
  }


  private static Coordinate[] shooter(int col, int row, int scale) {
    final String pathData =
        "M1.074,31.101l-1.074,-13.643l23.118,-9.389l4.27,-7.975l7.647,-0.094l4.581,"
            + "9.037l20.779,8.111l-1.262,13.002l-58.059,0.951Z";
    return toCoordinate(pathData, col, row, scale);
  }
}
