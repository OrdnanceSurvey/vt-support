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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class StringSummaryTest {

  @Test
  public void testSample() {
    String[] mission = new String[]{"We're", "passionate", "about", "the", "four", "core",
        "values", "that", "make", "up", "our", "organisation:", "we're", "adventurous,",
        "incisive,", "restless", "and", "true."};
    StringSummary summary = new StringSummary(mission);

    long expectedSamples = summary.getTotalSamples();
    long actualSamples = summary.getTotalSamples();
    assertEquals(expectedSamples, actualSamples);

    checkSampleWithinSize(summary);
    checkSampleIsSubsetOfInputValues(mission, summary);
  }

  private void checkSampleIsSubsetOfInputValues(String[] sampleInput, StringSummary summary) {
    Set<String> set = new HashSet<>(Arrays.asList(sampleInput));
    for (String value : summary.getSample()) {
      assertTrue("the sample contains a value that never went in!", set.contains(value));
    }
  }

  private void checkSampleWithinSize(StringSummary summary) {
    int length = 0;
    for (String s : summary.getSample()) {
      length += s.length();
    }
    assertTrue("Out of range: " + length, 0 < length && length <= 100);
  }

  @Test
  public void one() {
    String one = "one";

    StringSummary summary = new StringSummary(one);

    String expected = "one";
    String actual = summary.getValue();

    assertEquals(expected, actual);
  }

  @Test
  public void two() {
    String one = "one";
    String two = "two";

    StringSummary summary = new StringSummary(one, two);
    String expected = "one, two";
    check(summary, expected, 2);
  }

  @Test
  public void two_b() {
    String one = "one";
    String two = "two";

    StringSummary summary = new StringSummary(one);
    summary = new StringSummary(summary, two);
    String expected = "one, two";
    check(summary, expected, 2);

    // add another attribute with the same name.  For example, you have Alice and then we add
    // another Alice - the we only expect Alice to be included once _but_ we have an increased
    summary = new StringSummary(summary, one);
    check(summary, expected, 3);
  }

  @Test
  public void three() {
    String one = "one";
    String two = "two";
    String three = "three";

    StringSummary summary = new StringSummary(one, two, three);
    String expected = "one, three, two";
    check(summary, expected, 3);
  }

  @Test
  public void commasInValue() {
    String one = "something";
    String two = "nasty,comma";

    StringSummary summary = new StringSummary(one, two);
    String expected = "\"nasty,comma\", something";
    check(summary, expected, 2);
  }

  @Test
  public void commasInValues() {
    String one = "something, nasty";
    String two = "nasty,comma";

    StringSummary summary = new StringSummary(one, two);
    String expected = "\"nasty,comma\", \"something, nasty\"";
    check(summary, expected, 2);
  }

  private void check(StringSummary summary, String expectedValue, long expectedCount) {
    String actual = summary.getValue();
    assertEquals(expectedValue, actual);

    long actualCount = summary.getTotalSamples();
    assertEquals(expectedCount, actualCount);
  }

  @Test
  public void mergeSummaries() {
    String one = "something, nasty";
    String two = "nasty,comma";

    StringSummary summary = new StringSummary(one, two);

    String three = "something2, nasty2";
    String four = "nasty2,comma2";

    StringSummary summary2 = new StringSummary(three, four);

    StringSummary summary3 = new StringSummary(summary, summary2);

    String expected = "\"nasty,comma\", \"nasty2,comma2\", "
        + "\"something, nasty\", \"something2, nasty2\"";
    String actual = summary3.getValue();
    assertEquals(expected, actual);
  }

  @Test
  public void checkEquality() {
    EqualsVerifier.forClass(StringSummary.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void checkToString() {
    StringSummary summary = new StringSummary("a");
    String actual = summary.toString();
    String expected = "StringSummary{samples=1, sample=[a]}";
    assertEquals(expected, actual);

    summary = new StringSummary(summary, "b");
    actual = summary.toString();
    expected = "StringSummary{samples=2, sample=[a, b]}";
    assertEquals(expected, actual);
  }

  @Test
  public void checkHashcode() {
    StringSummary summary = new StringSummary("a");
    StringSummary summaryPrime = new StringSummary("a");
    assertEquals(summary.hashCode(), summaryPrime.hashCode());
  }

  @Test
  public void summaryFromString() {
    StringSummary summary = create(15, "apple", "orange");
    int expectedSize = 2;
    int actualSize = summary.getSample().size();
    assertEquals(expectedSize, actualSize);

    long expectedSamples = 15;
    long actualSamples = summary.getTotalSamples();
    assertEquals(expectedSamples, actualSamples);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkEmpty() {
    new StringSummary(new String[0]);
  }

  @Test(expected = NullPointerException.class)
  public void checkEmptyNull() {
    new StringSummary((String[])null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkEmptySummary() {
    new StringSummary(new StringSummary[0]);
  }

  @Test(expected = NullPointerException.class)
  public void checkEmptySummaryNull() {
    new StringSummary((StringSummary[])null);
  }

  @Test(expected = NullPointerException.class)
  public void checkTwoButOneNull() {
    String one = "one";

    StringSummary summary = new StringSummary(one);
    new StringSummary(summary, null);
  }

  @Test(expected = NullPointerException.class)
  public void checkTwoButOneSummaryNull() {
    StringSummary summary = new StringSummary("b");
    new StringSummary(summary, null);
  }

  @Test
  public void checkValue() {
    StringSummary summary = new StringSummary("High Street", "London Road", "Adanac Drive");
    String actual = summary.getValue();

    String expected = "Adanac Drive, High Street, London Road";
    assertEquals(expected, actual);
  }

  @Test
  public void checkSingleEmptyValue() {
    String road1 = "High Street";
    String road2 = "";
    String road3 = "London Road";

    StringSummary result = new StringSummary(road1, road2, road3);
    long expectedSamples = 3;
    long actualSamples = result.getTotalSamples();
    assertEquals(expectedSamples, actualSamples);

    int actualSampleSize = result.getSample().size();
    int expectedSampleSize = 2;
    assertEquals(expectedSampleSize, actualSampleSize);

    String actualValue = result.getValue();
    String expectedValue = "High Street, London Road";
    assertEquals(expectedValue, actualValue);
  }

  private StringSummary create(long total, String... values) {
    return new StringSummary(new TreeSet<>(Arrays.asList(values)), total);
  }
}
