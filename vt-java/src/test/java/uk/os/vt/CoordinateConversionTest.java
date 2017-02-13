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

package uk.os.vt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CoordinateConversionTest {

  private int[] getCoordinate(int zoom, int col, int row) {
    return new int[] {zoom, col, row};
  }

  @Test
  public void testLondonZ0() {
    final double[] expectedLatLon = london();

    final int[] expectedCoordinates = new int[] {0, 0, 0, 2046, 1362};
    final int[] actualCoordinates = CoordinateConversion.toTileCoordinates(expectedLatLon, 0);

    assertArrayEquals(expectedCoordinates, actualCoordinates);

    final double[] actualLatLon = CoordinateConversion.toLatLon(actualCoordinates);
    assertArrayEquals(expectedLatLon, actualLatLon, 0.05);
  }

  @Test
  public void testLondon() {
    // ~3km error - hyde park to trafalgar square
    // 51.50874245880333 -0.17578125
    testZoomLevel(london(), coords(0, 0, 0, 2046, 1362), 0, 0.05D);
    // ~400m error - Haymarket / Orange Street to trafalgar square
    // 51.50874245880333 -0.1318359375
    testZoomLevel(london(), coords(1, 0, 0, 4093, 2724), 1, 0.004D);
    // ~400m error - Haymarket / Orange Street to trafalgar square
    // 51.50874245880333 -0.1318359375
    testZoomLevel(london(), coords(2, 1, 1, 4090, 1352), 2, 0.004D);
    // 51.50874245880333 -0.1318359375
    testZoomLevel(london(), coords(3, 3, 2, 4084, 2704), 3, 0.004D);
    // 51.50874245880333 -0.1318359375
    testZoomLevel(london(), coords(4, 7, 5, 4072, 1312), 4, 0.004D);
    // 51.50874245880333 -0.12908935546875
    testZoomLevel(london(), coords(5, 15, 10, 4049, 2624), 5, 0.0011D);
    // 51.50874245880333 -0.12908935546875
    testZoomLevel(london(), coords(6, 31, 21, 4002, 1152), 6, 0.0011D);
    // 51.508315091920245 -0.1284027099609375
    testZoomLevel(london(), coords(7, 63, 42, 3909, 2305), 7, 0.0004D);
    // 51.50810140697543 -0.12805938720703125
    testZoomLevel(london(), coords(8, 127, 85, 3723, 515), 8, 0.0002D);
    // 51.50799456412721 -0.12805938720703125
    testZoomLevel(london(), coords(9, 255, 170, 3350, 1031), 9, 0.00004D);
    // 51.50799456412721 -0.12805938720703125
    testZoomLevel(london(), coords(10, 511, 340, 2604, 2062), 10, 0.00004D);
    // 51.50796785337601 -0.12805938720703125
    testZoomLevel(london(), coords(11, 1023, 681, 1112, 29), 11, 0.00004D);
    // 51.50796785337601 -0.1280379295349121
    testZoomLevel(london(), coords(12, 2046, 1362, 2225, 58), 12, 0.00002D);
    // 51.50796785337601 -0.12802720069885254
    testZoomLevel(london(), coords(13, 4093, 2724, 355, 116), 13, 0.000004D);
    // 51.507964514531004 -0.12802720069885254
    testZoomLevel(london(), coords(14, 8186, 5448, 710, 233), 14, 0.000004D);
  }

  @Test
  public void testSyndey() {
    // -33.79740876757247 151.171875
    testZoomLevel(sydney(), coords(0, 0, 0, 3768, 2457), 0, 0.07D);
    // -33.833920 151.171875
    testZoomLevel(sydney(), coords(1, 1, 1, 3440, 819), 1, 0.05D);
    // -33.852170 151.193848
    testZoomLevel(sydney(), coords(2, 3, 2, 2785, 1639), 2, 0.03D);
    // -33.852170 151.204834
    testZoomLevel(sydney(), coords(3, 7, 4, 1475, 3278), 3, 0.0098D);
    // -33.856732 151.210327
    testZoomLevel(sydney(), coords(4, 14, 9, 2951, 2461), 4, 0.0043D);
    // -33.856732 151.213074
    testZoomLevel(sydney(), coords(5, 29, 19, 1807, 826), 5, 0.0019D);
    // -33.857872 151.214447
    testZoomLevel(sydney(), coords(6, 58, 38, 3615, 1653), 6, 0.00068D);
    // -33.858442 151.214447
    testZoomLevel(sydney(), coords(7, 117, 76, 3134, 3307), 7, 0.00015D);
    // -33.858442 151.214447
    testZoomLevel(sydney(), coords(8, 235, 153, 2172, 2518), 8, 0.00015D);
    // -33.858442 151.214447
    testZoomLevel(sydney(), coords(9, 471, 307, 248, 940), 9, 0.00015D);
    // -33.858513 151.214533
    testZoomLevel(sydney(), coords(10, 942, 614, 497, 1881), 10, 0.000059D);
    // -33.858513 151.214576
    testZoomLevel(sydney(), coords(11, 1884, 1228, 995, 3762), 11, 0.000032D);
    // -33.858531 151.214576
    testZoomLevel(sydney(), coords(12, 3768, 2457, 1990, 3429), 12, 0.000016D);
    // -33.858540 151.214586
    testZoomLevel(sydney(), coords(13, 7536, 4915, 3981, 2763), 13, 0.000005D);
    // -33.858545 151.214586
    testZoomLevel(sydney(), coords(14, 15073, 9831, 3866, 1431), 14, 0.000005D);
  }

  @Test
  public void testTopLeftLatLon() {
    final int[] expectedGoogle = getCoordinate(1, 0, 0);

    final int inputZ = expectedGoogle[0];
    final int inputX = expectedGoogle[1];
    final int inputY = expectedGoogle[2];

    final double actualLat = CoordinateConversion.tile2lat(inputY, inputZ);
    final double actualLon = CoordinateConversion.tile2lon(inputX, inputZ);

    final double expectedLat = 85.05112;
    final double expectedLon = -180;
    final double delta = 0.00001;
    assertEquals(expectedLat, actualLat, delta);
    assertEquals(expectedLon, actualLon, delta);
  }

  @Test
  public void testTopLeftLatLonFraction() {
    final int inputZ = 1;
    final double inputX = 0.5D;
    final double inputY = 0.5D;

    final double actualLat = CoordinateConversion.tile2lat(inputY, inputZ);
    final double actualLon = CoordinateConversion.tile2lon(inputX, inputZ);

    final double expectedLat = 66.51326;
    final double expectedLon = -90;
    final double delta = 0.00001;
    assertEquals(expectedLat, actualLat, delta);
    assertEquals(expectedLon, actualLon, delta);
  }

  @Test
  public void testTopRightLatLonFraction() {
    final int inputZ = 1;
    final double inputX = 1.5D;
    final double inputY = 0.5D;

    final double actualLat = CoordinateConversion.tile2lat(inputY, inputZ);
    final double actualLon = CoordinateConversion.tile2lon(inputX, inputZ);

    final double expectedLat = 66.51326;
    final double expectedLon = 90;
    final double delta = 0.00001;
    assertEquals(expectedLat, actualLat, delta);
    assertEquals(expectedLon, actualLon, delta);
  }

  @Test
  public void testBlightyLatLonFraction() {
    final int inputZ = 9;
    final double inputX = 255.5D;
    final double inputY = 170.5D;

    final double actualLat = CoordinateConversion.tile2lat(inputY, inputZ);
    final double actualLon = CoordinateConversion.tile2lon(inputX, inputZ);

    final double expectedLat = 51.3992057;
    final double expectedLon = -0.3515625;
    final double delta = 0.00001;
    assertEquals(expectedLat, actualLat, delta);
    assertEquals(expectedLon, actualLon, delta);
  }

  @Test
  public void testBottomRightLatLon() {
    final int[] expectedGoogle = getCoordinate(1, 1, 1);

    final int inputZ = expectedGoogle[0];
    final int inputX = expectedGoogle[1];
    final int inputY = expectedGoogle[2];

    final double actualLat = CoordinateConversion.tile2lat(inputY, inputZ);
    final double actualLon = CoordinateConversion.tile2lon(inputX, inputZ);

    final double expectedLat = 0;
    final double expectedLon = 0;
    final double delta = 0.00001;
    assertEquals(expectedLat, actualLat, delta);
    assertEquals(expectedLon, actualLon, delta);
  }

  @Test
  public void testTmsTopLeft() {
    final int[] tms = getCoordinate(1, 0, 1);

    final int[] expectedGoogle = getCoordinate(1, 0, 0);
    final int[] actual = printGoogleConversion(tms);

    assertArrayEquals(expectedGoogle, actual);
  }

  @Test
  public void testTmsBottomRight() {
    final int[] tms = getCoordinate(1, 1, 0);

    final int[] expectedGoogle = getCoordinate(1, 1, 1);
    final int[] actual = printGoogleConversion(tms);

    assertArrayEquals(expectedGoogle, actual);
  }

  @Test
  public void testTmsLondonTrafalgarSquare() {
    final int[] tms = getCoordinate(21, 1047830, 1399799);

    final int[] expectedGoogle = getCoordinate(21, 1047830, 697352);
    final int[] actual = printGoogleConversion(tms);

    assertArrayEquals(expectedGoogle, actual);
  }

  @Test
  public void testTopLeftWithCoordinate() {
    final int[] tms = getCoordinate(1, 0, 1);

    final int[] expectedGoogle = getCoordinate(1, 0, 0);
    final int[] actual = printGoogleConversion(tms);

    assertArrayEquals(expectedGoogle, actual);
  }

  private int[] printGoogleConversion(int[] tms) {
    final int z = tms[0];
    final int x = tms[1];
    final int y = tms[2];
    final int[] google = CoordinateConversion.fromTms(z, x, y);
    return google;
  }

  private static double[] cairo() {
    return new double[] {30.042396, 31.229702};
  }

  private static double[] dublin() {
    return new double[] {53.347320, -6.259639};
  }

  private static double[] salisbury() {
    return new double[] {51.064712, -1.796172};
  }

  private static double[] rio() {
    return new double[] {-22.905156, -43.188553};
  }

  private static double[] sydney() {
    return new double[] {-33.858545, 151.214591};
  }

  private static double[] london() {
    return new double[] {51.507964, -0.128024};
  }

  private static double[] sanFrancisco() {
    return new double[] {37.808381, -122.409974};
  }

  private static double[] capeTown() {
    return new double[] {-33.906784, 18.422095};
  }

  private static double[] newYork() {
    return new double[] {40.766583, -73.977401};
  }

  private static double[] beijing() {
    return new double[] {39.918751, 116.396965};
  }

  private static int[] coords(int... params) {
    return params;
  }

  private static void testZoomLevel(double[] originalLatLon, int[] expectedCoordinates,
      int zoomLevel, double acceptableDelta) {
    final double[] expectedLatLon = originalLatLon;

    final int[] actualCoordinates =
        CoordinateConversion.toTileCoordinates(expectedLatLon, zoomLevel);
    assertArrayEquals(getMessageForArray(expectedCoordinates, actualCoordinates),
        expectedCoordinates, actualCoordinates);

    final double[] actualLatLon = CoordinateConversion.toLatLon(actualCoordinates);
    assertArrayEquals(getMessageForArray(expectedLatLon, actualLatLon), expectedLatLon,
        actualLatLon, acceptableDelta);
  }

  private static String getMessageForArray(int[] array, int[] array2) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      stringBuilder.append(array[i] + " (expected) -> " + array2[i] + "\n");
    }
    return stringBuilder.toString();
  }

  private static String getMessageForArray(double[] array, double[] array2) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      stringBuilder.append(array[i] + " (expected) -> " + array2[i] + "\n");
    }
    stringBuilder.append(
        "GeoJSON: \n" + array[1] + ", " + array[0] + "\n" + array2[1] + ", " + array2[0] + "\n");
    return stringBuilder.toString();
  }
}
