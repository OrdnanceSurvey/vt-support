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

package uk.os.vt.cluster.attribution;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

public class AttributeCombinerDefaultTest {

  @Test
  public void testDoubles() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object results = combiner.combine(4, 5);
    assertEquals(new DoubleSummary(4, 5), results);
  }

  @Test
  public void testDoubleSummaryDouble() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object results = combiner.combine(new DoubleSummary(1), 5);
    assertEquals(new DoubleSummary(5, 1, 2, 6), results);

    results = combiner.combine(5, new DoubleSummary(1));
    assertEquals(new DoubleSummary(5, 1, 2, 6), results);
  }

  @Test
  public void testDoubleSummaries() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object results = combiner.combine(new DoubleSummary(1), new DoubleSummary(2));
    assertEquals(new DoubleSummary(2, 1, 2, 3), results);
  }

  @Test
  public void testBooleans() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object result = combiner.combine(true, false);
    assertEquals(new BooleanSummary(1, 1, 2), result);
  }

  @Test
  public void testBooleanSummaryBoolean() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object result = combiner.combine(new BooleanSummary(true, true), false);
    assertEquals(new BooleanSummary(2, 1, 3), result);

    result = combiner.combine(false, new BooleanSummary(true, true));
    assertEquals(new BooleanSummary(2, 1, 3), result);
  }

  @Test
  public void testBooleanSummaries() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object result = combiner.combine(new BooleanSummary(true, true),
        new BooleanSummary(false, false));
    assertEquals(new BooleanSummary(2, 2, 4), result);
  }

  @Test
  public void testStrings() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object result = combiner.combine("Happy", "Days");
    assertEquals(new StringSummary("Happy", "Days"), result);
  }

  @Test
  public void testStringSummaryString() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object result = combiner.combine(new StringSummary("Happy", "Days"), "To Come");
    assertEquals(new StringSummary("Happy", "Days", "To Come"), result);

    result = combiner.combine("To Come", new StringSummary("Happy", "Days"));
    assertEquals(new StringSummary("Happy", "Days", "To Come"), result);
  }

  @Test
  public void testStringSummaries() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    Object result = combiner.combine(new StringSummary("Happy", "Days"),
        new StringSummary("To", "Come"));
    assertEquals(new StringSummary("Happy", "Days", "To", "Come"), result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnsupported() {
    AttributeCombinerDefault combiner = new AttributeCombinerDefault();
    combiner.combine(new IllegalArgumentException("unsupported!"),
        new IllegalArgumentException("unsupported!"));
  }
}
