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

public class BooleanSummaryTest {

  @Test
  public void checkConstructor() {
    long trues = 3;
    long falses = 5;
    long samples = 10;
    BooleanSummary booleanSummary = new BooleanSummary(trues, falses, samples);

    assertEquals(trues, booleanSummary.getTrues());
    assertEquals(falses, booleanSummary.getFalses());
    assertEquals(samples, booleanSummary.getSamples());
  }

  @Test
  public void checkConstructorVar() {
    BooleanSummary summary = new BooleanSummary(true);
    check(summary, 1, 0, 1);

    summary = new BooleanSummary(false);
    check(summary, 0, 1, 1);
  }

  @Test
  public void checkConstructorVar2() {
    BooleanSummary summary = new BooleanSummary(true, true);
    check(summary, 2, 0, 2);

    summary = new BooleanSummary(false, false);
    check(summary, 0, 2, 2);

    summary = new BooleanSummary(true, false);
    check(summary, 1, 1, 2);
  }

  @Test
  public void checkConstructorVar3() {
    BooleanSummary summary = new BooleanSummary(true, true, true);
    check(summary, 3, 0, 3);

    summary = new BooleanSummary(true, true, false);
    check(summary, 2, 1, 3);

    summary = new BooleanSummary(true, false, true);
    check(summary, 2, 1, 3);

    summary = new BooleanSummary(false, true, true);
    check(summary, 2, 1, 3);

    summary = new BooleanSummary(true, false, false);
    check(summary, 1, 2, 3);

    summary = new BooleanSummary(false, true, false);
    check(summary, 1, 2, 3);

    summary = new BooleanSummary(false, false, true);
    check(summary, 1, 2, 3);

    summary = new BooleanSummary(false, false, false);
    check(summary, 0, 3, 3);
  }

  @Test
  public void checkEquality() {
    EqualsVerifier.forClass(BooleanSummary.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void checkHashcode() {
    BooleanSummary summary = new BooleanSummary(true);
    BooleanSummary summaryPrime = new BooleanSummary(true);
    assertEquals(summary.hashCode(), summaryPrime.hashCode());
  }

  @Test
  public void checkToString() {
    BooleanSummary summary = new BooleanSummary(true);
    String actual = summary.toString();
    String expected = "BooleanSummary{trues=1, falses=0, samples=1}";
    assertEquals(expected, actual);

    summary = new BooleanSummary(summary, true);
    actual = summary.toString();
    expected = "BooleanSummary{trues=2, falses=0, samples=2}";
    assertEquals(expected, actual);
  }

  @Test
  public void checkToStringInverse() {
    BooleanSummary summary = new BooleanSummary(false);
    String actual = summary.toString();
    String expected = "BooleanSummary{trues=0, falses=1, samples=1}";
    assertEquals(expected, actual);

    summary = new BooleanSummary(summary, false);
    actual = summary.toString();
    expected = "BooleanSummary{trues=0, falses=2, samples=2}";
    assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void empty() {
    new BooleanSummary(new boolean[]{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyBooleanSummary() {
    new BooleanSummary(new BooleanSummary[]{});
  }

  @Test
  public void merge() {
    BooleanSummary summary = new BooleanSummary(true, false);
    BooleanSummary summary2 = new BooleanSummary(true);
    BooleanSummary summary3 = new BooleanSummary(summary, summary2);

    assertEquals(2, summary3.getTrues());
    assertEquals(1, summary3.getFalses());
    assertEquals(3, summary3.getSamples());
  }

  private void check(BooleanSummary summary, long expectedTrues, long expectedFalses,
                     long expectedSamples) {
    long actual = summary.getTrues();
    assertEquals(expectedTrues, actual);

    actual = summary.getFalses();
    assertEquals(expectedFalses, actual);

    actual = summary.getSamples();
    assertEquals(expectedSamples, actual);
  }
}
