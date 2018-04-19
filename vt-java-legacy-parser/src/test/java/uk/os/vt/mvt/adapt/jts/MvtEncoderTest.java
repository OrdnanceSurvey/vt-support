/*
 * Copyright (C) 2017 Weather Decision Technologies
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

package uk.os.vt.mvt.adapt.jts;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import uk.os.vt.mvt.adapt.jts.model.JtsLayer;
import uk.os.vt.mvt.adapt.jts.model.JtsMvt;

public final class MvtEncoderTest {

  private static final GeometryFactory GEOMETRY_FACORY = new GeometryFactory();

  private static JtsMvt decode(byte[] bytes) throws IOException {
    return MvtReader.loadMvt(new ByteArrayInputStream(bytes), GEOMETRY_FACORY,
        new TagKeyValueMapConverter());
  }

  @Test
  public void singleLayer() throws IOException {
    Collection<Geometry> geometries = PointGen.australia();

    JtsLayer layer = new JtsLayer("animals", geometries);
    JtsMvt mvt = new JtsMvt(singletonList(layer));

    final byte[] encoded = MvtEncoder.encode(mvt);
    assertEquals(mvt, decode(encoded));
  }

  @Test
  public void multipleLayers() throws IOException {
    JtsLayer layer = new JtsLayer("Australia", PointGen.australia());
    JtsLayer layer2 = new JtsLayer("United Kingdom", PointGen.uk());
    JtsLayer layer3 = new JtsLayer("United States of America", PointGen.usa());
    JtsMvt mvt = new JtsMvt(asList(layer, layer2, layer3));

    final byte[] encoded = MvtEncoder.encode(mvt);
    assertEquals(mvt, decode(encoded));
  }

  private static class PointGen {

    /**
     * Generate Geometries with this default specification.
     */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final Random RANDOM = new Random();

    private static Collection<Geometry> australia() {
      return getPoints(
          createPoint("Koala"),
          createPoint("Wombat"),
          createPoint("Platypus"),
          createPoint("Dingo"),
          createPoint("Croc"));
    }

    private static Collection<Geometry> uk() {
      return getPoints(
          createPoint("Hare"),
          createPoint("Frog"),
          createPoint("Robin"),
          createPoint("Fox"),
          createPoint("Hedgehog"),
          createPoint("Bulldog"));
    }

    private static Collection<Geometry> usa() {
      return getPoints(
          createPoint("Cougar"),
          createPoint("Raccoon"),
          createPoint("Beaver"),
          createPoint("Wolf"),
          createPoint("Bear"),
          createPoint("Coyote"));
    }

    private static Collection<Geometry> getPoints(Point... points) {
      return asList(points);
    }

    private static Point createPoint(String name) {
      Coordinate coord = new Coordinate(RANDOM.nextInt(4096), RANDOM.nextInt(4096));
      Point point = GEOMETRY_FACTORY.createPoint(coord);

      Map<String, Object> attributes = new LinkedHashMap<>();
      attributes.put("id", name.hashCode());
      attributes.put("name", name);
      point.setUserData(attributes);

      return point;
    }
  }
}
