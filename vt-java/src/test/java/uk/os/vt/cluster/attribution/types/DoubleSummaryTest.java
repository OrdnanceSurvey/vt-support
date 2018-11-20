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

package uk.os.vt.cluster.attribution.types;

import static org.junit.Assert.assertEquals;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import uk.os.vt.cluster.attribution.types.DoubleSummary;

public class DoubleSummaryTest {

  private static final double DELTA = 0.00001D;

  @Test
  public void first() {
    double max = 1D;
    double min = 1D;
    long samples = 2L;
    double sum = 2D;

    DoubleSummary summary = new DoubleSummary(max, min, samples, sum);
    DoubleSummary merged = new DoubleSummary(summary, 1L);

    DoubleSummary expected = new DoubleSummary(max, min, samples + 1, sum + 1);
    checkEquals(expected, merged);
  }

  @Test
  public void two() {
    double min = 10;
    double max = 55;

    DoubleSummary doubleSummary = new DoubleSummary(min, max);

    assertEquals(min, doubleSummary.getMin(), DELTA);
    assertEquals(max, doubleSummary.getMax(), DELTA);
    assertEquals(2, doubleSummary.getTotalSamples(), DELTA);
    assertEquals((min + max) / 2, doubleSummary.getMean(), DELTA);
  }

  @Test
  public void merge() {
    DoubleSummary doubleSummary = new DoubleSummary(15, 25);
    DoubleSummary doubleSummary2 = new DoubleSummary(103, 105);
    DoubleSummary doubleSummary3 = new DoubleSummary(doubleSummary, doubleSummary2);

    assertEquals(105, doubleSummary3.getMax(), DELTA);
    assertEquals(15, doubleSummary3.getMin(), DELTA);
    assertEquals(62, doubleSummary3.getMean(), DELTA);
    assertEquals(248, doubleSummary3.getSum(), DELTA);
    assertEquals(4, doubleSummary3.getTotalSamples(), DELTA);
  }

  @Test
  public void checkToString() {
    DoubleSummary doubleSummary = new DoubleSummary(15);

    String expected = "[max: 15.000000, min: 15.000000, samples: 1, sum: 15.000000]";
    String actual = doubleSummary.toString();
    assertEquals(expected, actual);

    DoubleSummary doubleSummary2 = new DoubleSummary(15, 25);

    expected = "[max: 25.000000, min: 15.000000, samples: 2, sum: 40.000000]";
    actual = doubleSummary2.toString();
    assertEquals(expected, actual);
  }

  @Test
  public void checkEquality() {
    EqualsVerifier.forClass(DoubleSummary.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkEmpty() {
    new DoubleSummary(new DoubleSummary[]{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkEmpty2() {
    new DoubleSummary((new double[]{}));
  }

  private void checkEquals(DoubleSummary one, DoubleSummary two) {
    assertEquals(one.getMax(), two.getMax(), DELTA);
    assertEquals(one.getMin(), two.getMin(), DELTA);
    assertEquals(one.getTotalSamples(), two.getTotalSamples());
    assertEquals(one.getSum(), two.getSum(), DELTA);
  }
}
