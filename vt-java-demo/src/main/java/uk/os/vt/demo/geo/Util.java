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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.util.LinkedHashMap;
import java.util.Map;

public class Util {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  static Point createPoint(String id, String name, double[] latlon) {
    return createPointXy(id, name, new double[]{latlon[1], latlon[0]});
  }

  static Point createPointXy(String id, String name, double[] coordinate) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("id", id.hashCode());
    attributes.put("name", name);
    attributes.put("timestamp", System.currentTimeMillis());

    Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(coordinate[0], coordinate[1]));
    point.setUserData(attributes);
    return point;
  }

  static Polygon createPolygonXy(String id, String name, double[][] coordinates) {
    Coordinate[] temp = new Coordinate[coordinates.length];

    for (int i = 0; i < coordinates.length; i++) {
      temp[i] = new Coordinate(coordinates[i][0], coordinates[i][1]);
    }

    Polygon polygon = GEOMETRY_FACTORY.createPolygon(temp);
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("id", id.hashCode());
    attributes.put("name", name);
    polygon.setUserData(attributes);
    return polygon;
  }
}
