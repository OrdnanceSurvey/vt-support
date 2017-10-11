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

import static uk.os.vt.demo.geo.Util.createPolygonXy;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Arrays;
import java.util.List;

public class Boxes {

  /**
   * All demo box geometry.
   *
   * @return all demo boxes
   */
  public static List<Geometry> all() {
    return Arrays.asList(createUpperLeftExtremeZ4(), createUpperRightExtremeZ4(),
        createLowerLeftExtremeZ4(), createLowerRightExtremeZ4());
  }

  /**
   * Box with extent of the upper left tile at zoom level 4.
   *
   * @return geometry
   */
  public static Polygon createUpperLeftExtremeZ4() {
    return createPolygonXy("ISO BOX", "Upper Left Box", new double[][]{
        {-180, 85.05112877980659},
        {-157.5, 85.05112877980659},
        {-157.5, 82.67628497834903},
        {-180, 82.67628497834903},
        {-180, 85.05112877980659}
    });
  }

  /**
   * Box with extent of the upper right tile at zoom level 4.
   *
   * @return geometry
   */
  public static Polygon createUpperRightExtremeZ4() {
    return createPolygonXy("ISO BOX", "Upper Right Box", new double[][]{
        {180, 85.05112877980659},
        {157.5, 85.05112877980659},
        {157.5, 82.67628497834903},
        {180, 82.67628497834903},
        {180, 85.05112877980659}
    });
  }

  /**
   * Box with extent of the lower left tile at zoom level 4.
   *
   * @return geometry
   */
  public static Polygon createLowerLeftExtremeZ4() {
    return createPolygonXy("ISO BOX", "Upper Left Box", new double[][]{
        {-180, -85.05112877980659},
        {-157.5, -85.05112877980659},
        {-157.5, -82.67628497834903},
        {-180, -82.67628497834903},
        {-180, -85.05112877980659}
    });
  }

  /**
   * Box with extent of the lower right tile at zoom level 4.
   *
   * @return geometry
   */
  public static Polygon createLowerRightExtremeZ4() {
    return createPolygonXy("ISO BOX", "Upper Right Box", new double[][]{
        {180, -85.05112877980659},
        {157.5, -85.05112877980659},
        {157.5, -82.67628497834903},
        {180, -82.67628497834903},
        {180, -85.05112877980659}
    });
  }
}
