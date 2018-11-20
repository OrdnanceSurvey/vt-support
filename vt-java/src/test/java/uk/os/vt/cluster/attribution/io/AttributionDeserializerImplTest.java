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

package uk.os.vt.cluster.attribution.io;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

public class AttributionDeserializerImplTest {

  private static final double DELTA = 0.00001D;

  @Test
  public void checkBooleanSummary() {
    long trues = 4;
    long falses = 5;
    long samples = 9;

    Map<String, Object> attr = toAttributes(
        "ratings:falses", falses,
        "ratings:trues", trues,
        "ratings:total", samples);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attr);

    BooleanSummary expected = new BooleanSummary(trues, falses, samples);
    assertEquals(expected, deserialized.get("ratings"));
  }

  @Test
  public void checkDoubleSummary() {
    Map<String, Object> attr = toAttributes(
        "age:max", 45D,
        "age:min", 32D,
        "age:sum", 77D,
        "age:total", 2L);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attr);

    DoubleSummary expected = new DoubleSummary(45, 32, 2, 77);
    assertEquals(expected, deserialized.get("age"));
  }

  @Test
  public void checkStringSummary() {
    Map<String, Object> attr = toAttributes(
        "words:subset", "Days,Happy",
        "words:total", 2L);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attr);
    StringSummary expected = new StringSummary(new String[]{"Happy", "Days"}, 2);

    assertEquals(expected, deserialized.get("words"));
  }

  @Test
  public void checkSimple() {
    Map<String, Object> attrs = toAttributes(
        "string", "String",
        "double", 46.7D,
        "boolean", true);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attrs);

    assertEquals("String", deserialized.get("string"));
    assertEquals(46.7D, (double) deserialized.get("double"), DELTA);
    assertEquals(true, (boolean) deserialized.get("boolean"));
  }

  @Test
  public void checkMultiple() {
    long trues = 4;
    long falses = 5;
    long samples = 9;
    Map<String, Object> attrs = toAttributes(
        "age:max", 45D,
        "age:min", 32D,
        "age:sum", 77D,
        "age:total", 2L,
        "name", "Edward",
        "ratings:falses", falses,
        "ratings:trues", trues,
        "ratings:total", samples,
        "words:subset", "Days,Happy",
        "words:total", 2L);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attrs);
    assertEquals(new DoubleSummary(45D, 32D, 2L, 77D), deserialized.get("age"));
    assertEquals("Edward", deserialized.get("name"));
    assertEquals(new BooleanSummary(trues, falses, samples), deserialized.get("ratings"));
    assertEquals(new StringSummary(new String[]{"Happy", "Days"}, 2), deserialized.get("words"));
  }


  @Test
  public void checkDoubleSummaryWithNull() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("price:max", null);
    attr.put("price:min", 10);
    attr.put("price:sum", 200);
    attr.put("price:total", 8);

    Map<String, Object> attr2 = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(attr2.isEmpty());

    attr.put("donated:max", 13);
    attr.put("donated:min", 1);
    attr.put("donated:sum", 85);
    attr.put("donated:total", 8);

    Map<String, Object> actual = AttributionDeserializerImpl.deserialize(attr);
    Map<String, Object> expected = new TreeMap<>();
    expected.put("donated", new DoubleSummary(13, 1, 8, 85));
    assertEquals(expected, actual);
  }

  @Test
  public void checkStringSummaryWithBadInput() {
    // invalid subset
    Map<String, Object> attr = toAttributes(
        "words:subset", null,
        "words:total", 2L);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());

    // invalid set
    attr = toAttributes("name:subset", "Adam,\"Zach", "name:total", 2L);
    deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());

    // invalid total
    attr = toAttributes(
        "words:subset", "Days,Happy",
        "words:total", null);
    deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());

    // invalid total (double)
    attr = toAttributes(
        "words:subset", "Days,Happy",
        "words:total", "two");
    deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());

    // float is mapped to long (cheaper than further checks) - we let this slip through atm
    attr = toAttributes(
        "words:subset", "Days,Happy",
        "words:total", 2.4);
    deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertEquals(new StringSummary(new String[]{"Happy", "Days"}, 2), deserialized.get("words"));
  }

  @Test
  public void checkDoubleSummaryWithBadInput() {
    Map<String, Object> attr = toAttributes(
        "age:max", "forty-five",
        "age:min", 32D,
        "age:sum", 77D,
        "age:total", 2L);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());

    attr = toAttributes(
        "age:max", 45,
        "age:min", 32D,
        "age:sum", 77D,
        "age:total", "TWO");
    deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());
  }

  @Test
  public void checkBooleanSummaryWithBadInput() {
    final long trues = 4;
    final long falses = 5;
    final long samples = 9;

    Map<String, Object> attr = toAttributes(
        "ratings:falses", "false",
        "ratings:trues", trues,
        "ratings:total", samples);

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertTrue(deserialized.isEmpty());

    // float is mapped to long (cheaper than further checks) - we let this slip through atm
    attr = toAttributes(
        "ratings:falses", 5.4,
        "ratings:trues", trues,
        "ratings:total", samples);
    deserialized = AttributionDeserializerImpl.deserialize(attr);
    assertEquals(new BooleanSummary(trues, falses, samples), deserialized.get("ratings"));
  }

  @Test
  public void checkNormalStringsWithKeysSimilarToSummaryPattern() {
    Map<String, Object> attrs = toAttributes(
        "name", "London",
        "name:es", "Londres");

    Map<String, Object> deserialized = AttributionDeserializerImpl.deserialize(attrs);
    assertEquals(attrs, deserialized);

  }

  private Map<String, Object> toAttributes(Object... obj) {
    if (obj.length % 2 != 0) {
      throw new IllegalArgumentException("ensure that both keys and values are specified");
    }

    Map<String, Object> attrs = new HashMap<>();
    for (int i = 0; i < obj.length; i += 2) {
      attrs.put((String) obj[i], obj[i + 1]);
    }
    return attrs;
  }
}
