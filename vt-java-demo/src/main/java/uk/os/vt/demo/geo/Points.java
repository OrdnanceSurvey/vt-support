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

import static uk.os.vt.demo.geo.Util.createPointXy;

import java.util.Arrays;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

public class Points {

  /**
   * All demo points.
   *
   * @return all demo points
   */
  public static List<Geometry> all() {
    return Arrays.asList(australia(), bermuda(), congo(), unitedKingdom(), unitedStatesOfAmerica());
  }

  /**
   * Point within Australia.
   *
   * @return geometry
   */
  public static Point australia() {
    return createPointXy("ISO AU", "Australia", new double[]{142.20703125, -10.919617760254685});
  }

  /**
   * Point within Bermuda.
   *
   * @return geometry
   */
  public static Point bermuda() {
    return createPointXy("ISO BM", "Bermuda", new double[]{-64.68681335449219, 32.407211836256685});
  }

  /**
   * Point within Democratic Republic of the Congo.
   *
   * @return geometry
   */
  public static Point congo() {
    return createPointXy("ISO CD", "Democratic Republic of Congo", new double[]{22.763671875,
        10.660607953624776});
  }

  /**
   * Point within the United Kingdom.
   *
   * @return geometry
   */
  public static Point unitedKingdom() {
    return createPointXy("ISO GB", "United Kingdom", new double[]{-1.470334, 50.938121});
  }

  /**
   * Point within the United States of America.
   *
   * @return geometry
   */
  public static Point unitedStatesOfAmerica() {
    return createPointXy("ISO US", "United States of America", new double[]{-93.69140625,
        49.15296965617042});
  }
}
