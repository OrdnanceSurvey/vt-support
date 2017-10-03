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

import com.vividsolutions.jts.geom.Geometry;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Outbreaks {

  private static final long START = Timestamp.valueOf("2010-01-01 00:00:00").getTime();
  private static final long END = System.currentTimeMillis();

  /**
   * All outbreak geometry.
   *
   * @return all demo outbreaks
   */
  public static List<Geometry> all() {
    return Arrays.asList(australia(), bermuda(), congo(), unitedKingdom(), unitedStatesOfAmerica());
  }

  /**
   * Polygon of Australia with Q Fever attributes.
   *
   * @return geometry
   */
  public static Geometry australia() {
    return apply("Q fever", Countries.australia());
  }

  /**
   * Polygon of Democratic Republic of the Congo with Yellow Fever attributes.
   *
   * @return geometry
   */
  public static Geometry congo() {
    return apply("Yellow Fever", Countries.congo());
  }

  /**
   * Polygon of Bermuda with Rabies attributes.
   *
   * @return geometry
   */
  public static Geometry bermuda() {
    return apply("Rabies", Countries.bermuda());
  }

  /**
   * Polygon of the United Kingdom with Legionnaires attributes.
   *
   * @return geometry
   */
  public static Geometry unitedKingdom() {
    return apply("Legionnaires' disease", Countries.unitedKingdom());
  }

  /**
   * Polygon of the United States of America West Nile Fever attributes.
   *
   * @return geometry
   */
  public static Geometry unitedStatesOfAmerica() {
    return apply("West Nile fever", Countries.unitedStatesOfAmerica());
  }

  private static Geometry apply(String disease, Geometry g) {
    Map<String, Object> attributes = (Map<String, Object>) g.getUserData();

    // create random timestamp
    long measurementSpace = END - START;
    long timestamp = START + (long) (Math.random() * measurementSpace);

    attributes.put("timestamp", timestamp);
    attributes.put("disease_name", disease);
    g.setUserData(attributes);
    return g;
  }
}
