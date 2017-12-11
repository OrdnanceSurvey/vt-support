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

package uk.os.vt.mvt.adapt.jts.model;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class JtsMvtTest {

  @Test
  public void testConstructor() {
    final JtsLayer layer1 = new JtsLayer("first");
    final JtsLayer layer2 = new JtsLayer("second");

    final JtsMvt mvt = new JtsMvt(layer1, layer2);
    assertTrue(mvt.getLayers().containsAll(asList(layer1, layer2)));
  }

  @Test
  public void testLayerByName() {
    final JtsLayer layer1 = new JtsLayer("first");
    final JtsLayer layer2 = new JtsLayer("second");

    final JtsMvt mvt = new JtsMvt(layer1, layer2);

    assertEquals(layer1, mvt.getLayer("first"));
    assertEquals(layer2, mvt.getLayer("second"));
  }

  @Test
  public void testEquality() {
    final JtsLayer layer1 = new JtsLayer("first");
    final JtsLayer layer2 = new JtsLayer("second");

    final JtsMvt mvt = new JtsMvt(layer1, layer2);

    final JtsLayer duplicateLayer1 = new JtsLayer("first");
    final JtsLayer duplicateLayer2 = new JtsLayer("second");

    final JtsMvt mvt2 = new JtsMvt(duplicateLayer1, duplicateLayer2);

    assertTrue(mvt.equals(mvt2));

    final JtsMvt mvt3 = new JtsMvt(duplicateLayer1, duplicateLayer2, new JtsLayer("extra"));

    assertFalse(mvt.equals(mvt3));
  }

  @Test
  public void testNoSuchLayer() {
    final JtsLayer layer = new JtsLayer("example");
    final JtsMvt mvt = new JtsMvt(layer);

    assertNull(mvt.getLayer("No Such Layer"));
  }
}
