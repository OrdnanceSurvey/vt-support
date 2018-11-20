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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

public class AttributionSerializerImplTest {

  @Test
  public void checkBooleanSummary() {
    long trues = 4;
    long falses = 5;
    long samples = 9;

    Map<String, Object> attributes = toAttributes("ratings", new BooleanSummary(trues, falses,
        samples));
    Map<String, Object> attributesSerialized = AttributionSerializerImpl.serialize(attributes);

    Map<String, Object> attributesExpected = toAttributes(
        "ratings:falses", falses,
        "ratings:trues", trues,
        "ratings:total", samples);

    assertEquals(attributesExpected, attributesSerialized);
  }

  @Test
  public void checkDoubleSummary() {
    Map<String, Object> attr = new HashMap<>();
    DoubleSummary attr3 = new DoubleSummary(45, 32, 2, 77);
    attr.put("age", attr3);

    Map<String, Object> attributesActual = AttributionSerializerImpl.serialize(attr);

    Map<String, Object> attributesExpected = toAttributes(
        "age:max", 45D,
        "age:min", 32D,
        "age:sum", 77D,
        "age:total", 2L);

    assertEquals(attributesExpected, attributesActual);
  }

  @Test
  public void checkStringSummary() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("words", new StringSummary("Happy", "Days"));

    Map<String, Object> attributesActual = AttributionSerializerImpl.serialize(attr);

    Map<String, Object> attributesExpected = toAttributes(
        "words:subset", "Days,Happy",
        "words:total", 2L);

    assertEquals(attributesExpected, attributesActual);
  }

  @Test
  public void checkSimple() {
    // no change special serialization occurs here
    Map<String, Object> attrs = toAttributes("name", "Edward");
    Map<String, Object> attributesActual = AttributionSerializerImpl.serialize(attrs);

    assertEquals(attrs, attributesActual);
  }

  @Test
  public void checkMultiple() {
    long trues = 4;
    long falses = 5;
    long samples = 9;
    Map<String, Object> attributesActual = AttributionSerializerImpl.serialize(toAttributes(
        "ratings", new BooleanSummary(trues, falses, samples),
        "age", new DoubleSummary(45, 32, 2, 77),
        "words", new StringSummary("Happy", "Days"),
        "name", "Edward"));

    Map<String, Object> attributesExpected = toAttributes(
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

    assertEquals(attributesExpected, attributesActual);
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
