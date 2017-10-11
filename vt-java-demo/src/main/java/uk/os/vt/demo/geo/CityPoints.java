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

package uk.os.vt.demo.geo;

import static uk.os.vt.demo.geo.Util.createPoint;

import com.vividsolutions.jts.geom.Point;

public class CityPoints {

  /**
   * Point within Beijing.
   *
   * @return geometry
   */
  public static Point beijing() {
    return createPoint("city-beijing", "Beijing", new double[]{39.918751, 116.396965});
  }

  /**
   * Point within Cairo.
   *
   * @return geometry
   */
  public static Point cairo() {
    return createPoint("city-cairo", "Cairo", new double[]{30.042396, 31.229702});
  }

  /**
   * Point within Cape Town.
   *
   * @return geometry
   */
  public static Point capeTown() {
    return createPoint("city-capeTown", "Cape Town", new double[]{-33.906784, 18.422095});
  }

  /**
   * Point within Dublin.
   *
   * @return geometry
   */
  public static Point dublin() {
    return createPoint("city-dublin", "Dublin", new double[]{53.347320, -6.259639});
  }


  /**
   * Point within London.
   *
   * @return geometry
   */
  public static Point london() {
    return createPoint("city-london", "London", new double[]{51.507964, -0.128024});
  }


  /**
   * Point within New York.
   *
   * @return geometry
   */
  public static Point newYork() {
    return createPoint("city-newYork", "New York", new double[]{40.766583, -73.977401});
  }


  /**
   * Point within Rio.
   *
   * @return geometry
   */
  public static Point rio() {
    return createPoint("city-rio", "Rio", new double[]{-22.905156, -43.188553});
  }

  /**
   * Point within Salisbury.
   *
   * @return geometry
   */
  public static Point salisbury() {
    return createPoint("city-salisbury", "Salisbury", new double[]{51.064712, -1.796172});
  }


  /**
   * Point within San Francisco.
   *
   * @return geometry
   */
  public static Point sanFrancisco() {
    return createPoint("city-sanFrancisco", "San Francisco", new double[]{37.808381, -122.409974});
  }

  /**
   * Point within Sydney.
   *
   * @return geometry
   */
  public static Point sydney() {
    return createPoint("city-sydney", "Sydney", new double[]{-33.858545, 151.214591});
  }

}
