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

package uk.os.vt.demo.art.xmas;

import static uk.os.vt.demo.art.Util.toCoordinate;

import org.locationtech.jts.geom.Coordinate;

public class Shapes {

  private Shapes(){}

  /**
   * Santa coordinates - best coloured red.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent an outline of santa
   */
  public static Coordinate[] santaBody(int col, int row, int scale) {
    final String pathData =
        "M143.752,153.795l-69.473,-59.342l-1.447,-7.237l1.447,-4.342l7.237,-4.342l8.684,"
            + "-2.895l7.237,4.342l10.132,13.027l1.447,-5.79l4.342,-4.342l2.895,1.448l0,"
            + "5.789l46.315,41.973l-20.263,-76.71l8.684,-26.052l10.132,-10.131l14.473,"
            + "-10.132l17.368,-5.789l21.711,-2.895l23.157,2.895l14.474,5.789l13.026,11.579l10.132,"
            + "14.473l8.684,21.711l5.789,24.605l2.895,15.921l1.447,4.342l-14.473,2.894l-2.895,"
            + "-13.026l-7.237,-18.815l-7.237,-8.684l-7.236,-7.237l13.026,66.578l10.131,8.684l7.237,"
            + "8.684l4.342,11.579l2.895,8.684l-2.895,13.026l-5.789,21.711l-123.025,8.684l-17.369,"
            + "-50.657Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Santa nose.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent a nose
   */
  public static Coordinate[] santaNose(int col, int row, int scale) {
    final String pathData = "M205.989,82.874l-7.237,1.448l-4.342,4.342l-1.448,"
        + "2.894l4.342,5.79l5.79,1.447l8.684,-2.171l4.342,-5.066l-1.447,-5.789l-8.684,-2.895Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Santa sack - best coloured brown.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent a sack for santa
   */
  public static Coordinate[] santaSack(int col, int row, int scale) {
    final String pathData =
        "M391.25,116.525l-20.263,-33.651l-39.079,-14.473l-5.789,-8.684l10.131,-17.369l-11.578,"
            + "2.895l-8.684,-11.579l-10.132,11.579l-11.579,-2.895l2.895,17.369l-7.237,"
            + "10.131l-15.921,20.987l-2.894,11.578l-7.237,107.828l47.762,15.921l68.026,"
            + "-28.947l11.579,-80.69Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Santa sledge.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent a sledge
   */
  public static Coordinate[] santaSledge(int col, int row, int scale) {
    final String pathData = "M391.25,236.294l-23.158,10.131l-78.17,7.974l-91.296,0.294l-31.61,"
        + "-1.934l-26.158,-7.781l-14.474,-7.237l-13.026,-7.237l-8.684,-8.684l-5.79,-13.026l-1.447,"
        + "-14.474l1.447,-15.92l8.685,-15.921l5.789,-7.237l28.947,5.789l4.342,8.684l14.474,"
        + "10.132l30.394,7.237l65.131,1.447l52.105,-8.684l39.078,-14.474l14.474,-7.236l10.131,"
        + "-7.237l7.237,-8.684l14.474,-24.605l7.236,14.473l2.895,18.816l1.447,14.473l1.448,"
        + "15.921l-1.448,8.684l-1.447,15.921l-1.447,13.026l-11.579,17.369Z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * The sleigh runners - best coloured black.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent an outline of santa
   */
  public static Coordinate[][] santaSledgeRunners(int col, int row, int scale) {
    final String one = "M 150 250 L 0 0 L -8 26";
    final String two = "M 250 260 L 0 0 L -8 20";
    final String sledge = "M757.034,274.979 -317.934,6.491 -348.236,1.377c-10.814,"
        + "-1.825 -23.932,-4.986 -32.853,-11.738c-7.694,-5.823 -18.506,-29.861 -18.506,"
        + "-29.861l-4.689,-19.49l10.418,-30.941l23.29,-19.273l36.374,4.099l13.708,11.405l2.719,"
        + "13.763l-0.52,22.272l-15.907,15.36l-19.92,4.155l-16.953,-17.022l-0.38,"
        + "-14.633l16.376,-18.3";
    return new Coordinate[][]{
        toCoordinate(sledge, col, row, scale),
        toCoordinate(one, col, row, scale),
        toCoordinate(two, col, row, scale)
    };
  }

  /**
   * Santa's visor.
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent a visor
   */
  public static Coordinate[] santaVisor(int col, int row, int scale) {
    final String pathData = "M 253.383 65.401 l -97.815 19.473 l 3.895 15.132 l 4.724 6.302"
        + " l 9.955 2 l 9.412 -3 l 5.763 -6.684 l 3.645 -11.408 l 4.342 -4.342 l 5.79 -1.447 "
        + "l 8.684 0 l 5.237 2.447 l 6.098 8.237 l 13.539 3 l 11.905 -2.195 l 7.063 -5.489 "
        + "l 1 -6.049 l -1 -6.339 l -2.237 -9.638 z";
    return toCoordinate(pathData, col, row, scale);
  }

  /**
   * Santa's cuffs, bobble, beard etc.  Best coloured white :)
   * @param col the x to offset
   * @param row the y to offset
   * @param scale the multiplier
   * @return the coordinates to represent santa's trimmings
   */
  public static Coordinate[][] santaTrimmings(int col, int row, int scale) {
    final String left = "M 190.068 90.111 l -14.474 1.447 l -10.131 7.237 l -14.474 3.618 "
        + "l -18.815 -6.513 l 20.263 21.711 l 15.92 2.894 l 15.198 -2.894 l 7.96 -5.79 "
        + "l 8.684 -5.789 l -3.619 0 l -4.342 -3.618 l -2.895 -4.342 l -2.894 -4.342 z";
    final String right = "M 220.462 85.769 l 14.474 1.447 l 10.131 7.237 l 13.026 -0.724 "
        + "l 18.816 -13.75 l -11.579 22.434 l -8.684 6.514 l -14.474 4.342 l -15.197 0 "
        + "l -7.96 -5.79 l -8.684 -5.789 l 4.342 -5.066 l 2.894 -6.513 l 2.895 -4.342 z";
    final String bobble = "M 281.251 96.624 l -8.684 5.789 l -3.618 15.198 l 0 5.789 "
        + "l 5.065 7.237 l 7.237 5.789 l 13.026 -1.447 l 7.237 -4.342 l 4.342 -10.132 l 0 -10.131 "
        + "l -4.342 -7.961 l -13.026 -5.065 l -7.237 -0.724 z";
    final String hatFluff = "M262.435,33.664l-130.261,24.605l4.342,27.5l131.709,"
        + "-26.052l-5.79,-26.053Z";
    final String cuff = "M294.277,169.715l-39.078,-15.92l-11.579,23.157l44.868,"
        + "17.368l5.789,-24.605Z";
    final String cuff2 = "M 136.516 110.374 l -17.369 20.263 l -21.71 -17.368 l 20.263 -23.158 "
        + "l 18.816 20.263 z";
    final String beard = "M207.436,105.308l-21.71,15.197l-14.474,2.895l-27.5,-5.789l31.842,"
        + "44.868l14.474,7.236l14.473,2.895l15.921,-1.447l21.71,-8.684l7.237,-7.237l13.026,"
        + "-15.921l13.027,-42.697l-13.027,12.303l-8.684,2.894l-13.026,4.704l-11.579,0l-7.236,"
        + "-3.256l-14.474,-7.961Z";
    return new Coordinate[][]{
        toCoordinate(left, col, row, scale),
        toCoordinate(right, col, row, scale),
        toCoordinate(cuff, col, row, scale),
        toCoordinate(cuff2, col, row, scale),
        toCoordinate(hatFluff, col, row, scale),
        toCoordinate(beard, col, row, scale),
        toCoordinate(bobble, col, row, scale)
    };
  }
}
