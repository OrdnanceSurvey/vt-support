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
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Test;

public class AttributionClustererTest {

  @Test
  public void stringSummarySerializeDeserialize() {
    StringSummary value = new StringSummary("Adam", "Zach");
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", value);

    Map<String, Object> values = AttributionClusterer.serialize(attr);
    assertTrue(values.containsKey("name:subset"));
    assertTrue(values.containsKey("name:total"));

    Map<String, Object> deserialized = AttributionClusterer.deserialize(values);
    assertEquals(attr, deserialized);
  }

  @Test
  public void stringSummaryWithStringSummary() {
    Map<String, Object> attr = new HashMap<>();
    StringSummary value = new StringSummary(new String[]{"Tom", "Jerry"}, 2);
    attr.put("duo", value);

    Map<String, Object> attr2 = new HashMap<>();
    // implicitly two items
    StringSummary value2 = new StringSummary("Bert", "Ernie");
    attr2.put("duo", value2);

    Map<String, Object> result = new AttributionClusterer.Builder().build().combine(attr, attr2);

    StringSummary summary = (StringSummary) result.get("duo");
    StringSummary expected = new StringSummary("Bert", "Ernie", "Jerry", "Tom");
    assertEquals(expected, summary);
  }

  @Test
  public void stringWithStringSummary() {
    // explicitly one item
    StringSummary input1 = new StringSummary(new String[]{"Leonard"}, 1);
    // implicitly two items
    StringSummary input2 = new StringSummary("Bert", "Ernie");
    // implicitly three items
    StringSummary expected = new StringSummary("Bert", "Ernie", "Leonard");

    Map<String, Object> attr = new HashMap<>();
    attr.put("names", input1);
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("names", input2);

    Map<String, Object> result = new AttributionClusterer.Builder().build().combine(attr, attr2);
    StringSummary actual = (StringSummary) result.get("names");
    assertEquals(expected, actual);
  }

  @Test
  public void stringWithString() {
    String input1 = "Read";
    String input2 = "Leonard";
    StringSummary expected = new StringSummary("Leonard", "Read");

    Map<String, Object> attr = new HashMap<>();
    attr.put("names", input1);
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("names", input2);

    Map<String, Object> result = new AttributionClusterer.Builder().build().combine(attr, attr2);
    StringSummary summary = (StringSummary) result.get("names");
    assertEquals(expected, summary);
  }

  @Test
  public void booleanSummary() {
    BooleanSummary value = new BooleanSummary(5, 3, 8);
    Map<String, Object> attr = new HashMap<>();
    attr.put("open", value);

    Map<String, Object> values = AttributionClusterer.serialize(attr);
    assertTrue(values.containsKey("open:trues"));
    assertTrue(values.containsKey("open:falses"));
    assertTrue(values.containsKey("open:total"));

    Map<String, Object> deserialized = AttributionClusterer.deserialize(values);
    assertEquals(attr, deserialized);
  }

  @Test
  public void doubleSummaryWithExcludes() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("age:max", 70);
    attr.put("age:min", 30);
    attr.put("age:sum", 100);
    attr.put("age:total", 2);
    attr.put("height:max", 171);
    attr.put("height:min", 163);
    attr.put("height:sum", 334);
    attr.put("height:total", 2);
    attr.put("name", "Alice,Becky");


    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("age", 45);
    attr2.put("height", 192);
    attr2.put("name", "Billy");

    AttributionClusterer clusterer = new AttributionClusterer.Builder()
        .excludeKey("id")
        .excludeKey("name")
        .excludeKey("attribute_include")
        .build();

    Map<String, Object> unpackaged = AttributionClusterer.deserialize(attr);
    Map<String, Object> result = clusterer.combine(unpackaged, attr2);

    assertEquals(3, unpackaged.size()); // height, age, name
    assertEquals(2, result.size()); // height and age - because name is excluded
    assertTrue(unpackaged.containsKey("height"));
    assertTrue(unpackaged.containsKey("age"));

    assertEquals(new DoubleSummary(192, 163, 3, 526), result.get("height"));
    assertEquals(new DoubleSummary(70, 30, 3, 145), result.get("age"));
  }

  @Test
  public void testSerializationAndDeserialization() throws IOException {
    // attributes to serialise
    Map<String, Object> attr = new HashMap<>();

    // some strings
    StringSummary attr1 = new StringSummary("Adam", "Zach");
    attr.put("name", attr1);

    // some booleans
    BooleanSummary attr2 = new BooleanSummary(1, 1, 2);
    attr.put("working", attr2);

    // some numbers
    DoubleSummary attr3 = new DoubleSummary(45, 32, 2, 77);
    attr.put("age", attr3);

    Map<String, Object> values = AttributionClusterer.serialize(attr);

    // check strings attribute keys
    assertTrue(values.containsKey("name:subset"));
    assertTrue(values.containsKey("name:total"));
    // check boolean attribute keys
    assertTrue(values.containsKey("working:trues"));
    assertTrue(values.containsKey("working:falses"));
    assertTrue(values.containsKey("working:total"));
    // check number attribute keys
    assertTrue(values.containsKey("age:max"));
    assertTrue(values.containsKey("age:min"));
    assertTrue(values.containsKey("age:sum"));
    assertTrue(values.containsKey("age:total"));

    // check string attribute values
    assertEquals("Adam,Zach", values.get("name:subset"));
    assertEquals(2L, values.get("name:total"));

    // check boolean attribute values
    assertEquals(1L, values.get("working:trues"));
    assertEquals(1L, values.get("working:falses"));
    assertEquals(2L, values.get("working:total"));

    // check number attribute values
    assertEquals(45D, values.get("age:max"));
    assertEquals(32D, values.get("age:min"));
    assertEquals(77D, values.get("age:sum"));
    assertEquals(2L, values.get("age:total"));

    // check restoration equals what went in!
    Map<String, Object> backAgain = AttributionClusterer.deserialize(values);
    assertEquals(attr, backAgain);
  }

  @Test
  public void mergeSingleWithClusterDouble() {
    // first (cluster)
    Map<String, Object> attr = new HashMap<>();
    DoubleSummary attr3 = new DoubleSummary(45, 32, 2, 77);
    attr.put("age", attr3);
    Map<String, Object> values = AttributionClusterer.serialize(attr);
    Map<String, Object> attrAgain = AttributionClusterer.deserialize(values);

    // second (single)
    Map<String, Object> attr2 = new HashMap<>();
    double attr4 = 47;
    attr2.put("age", attr4);
    Map<String, Object> values2 = AttributionClusterer.serialize(attr2);
    Map<String, Object> attr2Again = AttributionClusterer.deserialize(values2);

    Map<String, Object> result = new AttributionClusterer.Builder().build()
        .combine(attrAgain, attr2Again);

    DoubleSummary expected = new DoubleSummary(47, 32, 3, 124);
    assertEquals(expected, result.get("age"));
  }

  @Test
  public void mergeSingleWithClusterString() {
    // first (cluster)
    Map<String, Object> attr = new HashMap<>();
    StringSummary attr3 = create(2, "Happy", "Days");
    attr.put("words", attr3);
    Map<String, Object> values = AttributionClusterer.serialize(attr);
    Map<String, Object> attrAgain = AttributionClusterer.deserialize(values);

    // second (single)
    Map<String, Object> attr2 = new HashMap<>();
    String attr4 = "Sir";
    attr2.put("words", attr4);
    Map<String, Object> values2 = AttributionClusterer.serialize(attr2);
    Map<String, Object> attr2Again = AttributionClusterer.deserialize(values2);

    Map<String, Object> result = new AttributionClusterer.Builder().build()
        .combine(attrAgain, attr2Again);

    StringSummary expected = create(3, "Happy", "Days", "Sir");
    assertEquals(expected, result.get("words"));

    // and now single then cluster
    attrAgain = AttributionClusterer.deserialize(values);
    result = new AttributionClusterer.Builder().build().combine(attr2Again, attrAgain);
    expected = create(3, "Sir", "Happy", "Days");
    assertEquals(expected, result.get("words"));
  }

  @Test
  public void mergeNumberWithNumber() {
    Number input1 = 15;
    Number input2 = 35.5;
    DoubleSummary expected = new DoubleSummary(35.5D, 15D, 2L, 50.5D);

    Map<String, Object> attr = new HashMap<>();
    attr.put("degrees", input1);
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("degrees", input2);

    Map<String, Object> result = new AttributionClusterer.Builder().build().combine(attr, attr2);
    DoubleSummary summary = (DoubleSummary) result.get("degrees");
    assertEquals(expected, summary);
  }

  @Test
  public void mergeNumberWithDoubleSummary() {
    Number input1 = 15;
    DoubleSummary input2 = new DoubleSummary(90, 45, 2, 135);
    DoubleSummary expected = new DoubleSummary(90, 15, 3, 150);

    Map<String, Object> attr = new HashMap<>();
    attr.put("degrees", input1);
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("degrees", input2);

    Map<String, Object> result = new AttributionClusterer.Builder().build().combine(attr, attr2);
    DoubleSummary summary = (DoubleSummary) result.get("degrees");
    assertEquals(expected, summary);
  }

  @Test
  public void mergeDoubleSummaryWithDoubleSummary() {
    DoubleSummary input1 = new DoubleSummary(180, 46, 4, 380);
    DoubleSummary input2 = new DoubleSummary(90, 45, 2, 135);
    DoubleSummary expected = new DoubleSummary(180, 45, 6, 515);

    Map<String, Object> attr = new HashMap<>();
    attr.put("degrees", input1);
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("degrees", input2);

    Map<String, Object> result = new AttributionClusterer.Builder().build().combine(attr, attr2);
    DoubleSummary summary = (DoubleSummary) result.get("degrees");
    assertEquals(expected, summary);
  }

  @Test
  public void mergeBooleanClusters() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("working", new BooleanSummary(5, 3, 8));

    Map<String, Object> attr1 = new HashMap<>();
    attr1.put("working", new BooleanSummary(6, 80, 86));

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder().build();
    Map<String, Object> result = attributionClusterer.combine(attr, attr1);

    BooleanSummary expected = new BooleanSummary(11, 83, 94);
    assertEquals(expected, result.get("working"));
  }

  @Test
  public void mergeBooleanWithBooleanCluster() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("working", true);

    Map<String, Object> attr1 = new HashMap<>();
    attr1.put("working", new BooleanSummary(6, 80, 86));

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder().build();
    Map<String, Object> result = attributionClusterer.combine(attr, attr1);

    BooleanSummary expected = new BooleanSummary(7, 80, 87);
    assertEquals(expected, result.get("working"));
  }

  @Test
  public void mergeBooleanWithBoolean() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("working", true);

    Map<String, Object> attr1 = new HashMap<>();
    attr1.put("working", false);

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder().build();
    Map<String, Object> result = attributionClusterer.combine(attr, attr1);

    BooleanSummary expected = new BooleanSummary(1, 1, 2);
    assertEquals(expected, result.get("working"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void mergeBooleanWithUnknown() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("working", true);

    Map<String, Object> attr1 = new HashMap<>();
    attr1.put("working", new StringSummary("should break"));

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder().build();
    attributionClusterer.combine(attr, attr1);
  }

  @Test
  public void mergeSingleWithClusterBoolean() {
    int initialTrues = 3;
    int intialFalses = 1;
    int initialTotal = initialTrues + intialFalses;

    Map<String, Object> attr = new HashMap<>();
    attr.put("working", new BooleanSummary(initialTrues, intialFalses, initialTotal));

    Map<String, Object> attr1 = new HashMap<>();
    attr1.put("working", true);

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder().build();
    Map<String, Object> result = attributionClusterer.combine(attr, attr1);

    BooleanSummary expected = new BooleanSummary(initialTrues + 1, intialFalses, initialTotal + 1);
    assertEquals(expected, result.get("working"));
  }

  @Test(expected = NullPointerException.class)
  public void testNullMerger() {

    Map<String, Object> attr = new HashMap<>();
    attr.put("working_hours", 7.5);

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("working_hours", null);

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder().build();
    attributionClusterer.combine(attr, attr2);
  }

  @Test
  public void customCombiner2() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("recommended", new Customer("Ben", 1951));

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("recommended", new Customer("Jerry", 1951));

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .combiner(new AttributionClusterer.CustomCombiner() {
          @Override
          public Object combine(Object existing, Object item) {
            if (existing instanceof CustomerSummary) {
              if (item instanceof Customer) {
                return new CustomerSummary((CustomerSummary) existing, (Customer) item);
              } else if (item instanceof CustomerSummary) {
                return new CustomerSummary((CustomerSummary) existing, (CustomerSummary) item);
              }
            }

            if (existing instanceof Customer) {
              if (item instanceof Customer) {
                return new CustomerSummary((Customer)existing, (Customer) item);
              } else if (item instanceof CustomerSummary) {
                return new CustomerSummary((CustomerSummary) item, (Customer) existing);
              }
            }
            throw new IllegalArgumentException("Cannot combine: \"" + existing + "\" with \""
                + item + "\"");
          }
        }, Customer.class, CustomerSummary.class)
        .build();

    Map<String, Object> result = attributionClusterer.combine(attr, attr2);
    CustomerSummary expected = new CustomerSummary(2);
    assertEquals(expected, result.get("recommended"));
  }

  @Test
  public void customCombiner() {
    // Custom clustering:
    //  - normal processing for name
    //  - custom processing for location and season
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", "Ben");
    attr.put("location", "Southampton, United Kingdom");
    attr.put("season", "Winter");

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("name", "Jerry");
    attr2.put("location", "Sydney, Australia");
    attr2.put("season", "Summer");

    AttributionClusterer.CustomCombiner colonCombiner = (existing, item) -> {
      if (existing instanceof String && item instanceof String) {
        return String.format("%s : %s", existing, item);
      }
      return existing;
    };
    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .addCombiner(colonCombiner, "location", "season")
        .build();
    Map<String, Object> actual = attributionClusterer.combine(attr, attr2);
    Map<String, Object> expected = getAttributes(new StringSummary("Ben", "Jerry"),
        "Southampton, United Kingdom : Sydney, Australia",
        "Winter : Summer");
    assertEquals(expected, actual);
  }

  private Map<String, Object> getAttributes(StringSummary stringSummary, String location,
                                            String season) {
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", stringSummary);
    attr.put("location", location);
    attr.put("season", season);
    return attr;
  }

  @Test
  public void customCombinerExclude() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", "Ben");
    attr.put("age", 57);
    attr.put("favourite_color", "sky blue");

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("name", "Jerry");
    attr2.put("age", 53);
    attr2.put("favourite_color", "hot pink");

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .excludeKey("name")
        .build();

    Map<String, Object> actual = attributionClusterer.combine(attr, attr2);
    Map<String, Object> expected = new HashMap<>();
    expected.put("favourite_color", new StringSummary("hot pink", "sky blue"));
    expected.put("age", new DoubleSummary(57, 53, 2, 110));
    assertEquals(expected, actual);
  }

  @Test
  public void customCombinerIncludeExclude() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", "Ben");
    attr.put("age", 57);
    attr.put("favourite_color", "sky blue");

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("name", "Jerry");
    attr2.put("age", 53);
    attr2.put("favourite_color", "hot pink");

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .includeKeys("age", "favourite_color")
        .excludeKey("name")
        .build();

    Map<String, Object> actual = attributionClusterer.combine(attr, attr2);
    Map<String, Object> expected = new HashMap<>();
    expected.put("favourite_color", new StringSummary("hot pink", "sky blue"));
    expected.put("age", new DoubleSummary(57, 53, 2, 110));
    assertEquals(expected, actual);
  }

  @Test
  public void customCombinerIncludeAndExcludeThenExcludeOverrides() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", "Ben");
    attr.put("age", 57);
    attr.put("favourite_color", "sky blue");

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("name", "Jerry");
    attr2.put("age", 53);
    attr2.put("favourite_color", "hot pink");

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .includeKeys("name", "age", "favourite_color", "not_there")
        .excludeKey("name")
        .build();

    Map<String, Object> actual = attributionClusterer.combine(attr, attr2);
    Map<String, Object> expected = new HashMap<>();
    expected.put("favourite_color", new StringSummary("hot pink", "sky blue"));
    expected.put("age", new DoubleSummary(57, 53, 2, 110));
    assertEquals(expected, actual);
  }

  @Test
  public void customCombinerIncludeAndExcludeExtraNotIncludedField() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("name", "Ben");
    attr.put("age", 57);
    attr.put("favourite_color", "sky blue");

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("name", "Jerry");
    attr2.put("age", 53);
    attr2.put("favourite_color", "hot pink");
    attr2.put("favourite_ice_cream", "Coconut Almond Fudge Chip");

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .includeKeys("name", "age", "favourite_color", "not_there")
        .excludeKey("name")
        .build();

    Map<String, Object> actual = attributionClusterer.combine(attr, attr2);
    Map<String, Object> expected = new HashMap<>();
    expected.put("favourite_color", new StringSummary("hot pink", "sky blue"));
    expected.put("age", new DoubleSummary(57, 53, 2, 110));
    assertEquals(expected, actual);
  }

  @Test
  public void badDeserialzationBoolean() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("feedback:falses", "five");
    attr.put("feedback:trues", 3);
    attr.put("feedback:total", 8);

    Map<String, Object> attr2 = AttributionClusterer.deserialize(attr);
    assertTrue(attr2.isEmpty());

    attr.put("donated:falses", 40);
    attr.put("donated:trues", 20);
    attr.put("donated:total", 60);

    Map<String, Object> actual = AttributionClusterer.deserialize(attr);
    Map<String, Object> expected = new TreeMap<>();
    expected.put("donated", new BooleanSummary(20, 40, 60));
    assertEquals(expected, actual);
  }

  @Test
  public void badDeserialzationString() {
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("fruit:subset", null);
    attr2.put("fruit:total", 2);

    Map<String, Object> attr = AttributionClusterer.deserialize(attr2);
    assertTrue(attr.isEmpty());

    attr2.put("tools:subset", "trowel,seed drill,plough");
    attr2.put("tools:total", 10);

    Map<String, Object> actual = AttributionClusterer.deserialize(attr2);
    Map<String, Object> expected = new TreeMap<>();
    expected.put("tools", new StringSummary(new String[]{"trowel", "seed drill", "plough"}, 10));
    assertEquals(expected, actual);
  }

  @Test
  public void badDeserialzationLong() {
    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("fruit:subset", "apples,oranges");
    attr2.put("fruit:total", "six");

    Map<String, Object> attr = AttributionClusterer.deserialize(attr2);
    assertTrue(attr.isEmpty());

    attr2.put("tools:subset", "trowel,seed drill,plough");
    attr2.put("tools:total", 10);

    Map<String, Object> actual = AttributionClusterer.deserialize(attr2);
    Map<String, Object> expected = new TreeMap<>();
    expected.put("tools", new StringSummary(new String[]{"trowel", "seed drill", "plough"}, 10));
    assertEquals(expected, actual);
  }

  @Test
  public void badDeserialzationDouble() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("price:max", "Fifty");
    attr.put("price:min", 10);
    attr.put("price:sum", 200);
    attr.put("price:total", 8);

    attr.put("donated:max", 13);
    attr.put("donated:min", 1);
    attr.put("donated:sum", 85);
    attr.put("donated:total", 8);

    Map<String, Object> actual = AttributionClusterer.deserialize(attr);
    Map<String, Object> expected = new TreeMap<>();
    expected.put("donated", new DoubleSummary(13, 1, 8, 85));
    assertEquals(expected, actual);
  }

  @Test
  public void badDeserialzationDoubleNull() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("price:max", null);
    attr.put("price:min", 10);
    attr.put("price:sum", 200);
    attr.put("price:total", 8);

    Map<String, Object> attr2 = AttributionClusterer.deserialize(attr);
    assertTrue(attr2.isEmpty());

    attr.put("donated:max", 13);
    attr.put("donated:min", 1);
    attr.put("donated:sum", 85);
    attr.put("donated:total", 8);


    Map<String, Object> actual = AttributionClusterer.deserialize(attr);
    Map<String, Object> expected = new TreeMap<>();
    expected.put("donated", new DoubleSummary(13, 1, 8, 85));
    assertEquals(expected, actual);
  }

  @Test
  public void testDeserializationInvalidSet() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("name:subset", "Adam,\"Zach");
    attr.put("name:total", 2);
    Map<String, Object> attr2 = AttributionClusterer.deserialize(attr);
    assertTrue(attr2.isEmpty());
  }

  @Test
  public void testNearSerialisationAttributeName() {
    Map<String, Object> attr1 = new HashMap<>();
    attr1.put("name:en", "London");

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("name:en", "Southampton");


    AttributionClusterer clusterer = new AttributionClusterer.Builder().build();

    Map<String, Object> actual = clusterer.combine(attr1, attr2);
    StringSummary expected = new StringSummary(new String[]{"London", "Southampton"}, 2);
    assertEquals(expected, actual.get("name:en"));


    Map<String, Object> attr3 = new HashMap<>();
    attr3.put("name:en:subset", "London,Southampton");
    attr3.put("name:en:total", 2L);
    actual = AttributionClusterer.deserialize(attr3);
    assertEquals(expected, actual.get("name:en"));
  }

  private StringSummary create(long total, String... values) {
    return new StringSummary(new TreeSet<>(Arrays.asList(values)), total);
  }

  private static class Customer {
    private final String name;
    private final int age;

    public Customer(String name, int age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public String toString() {
      return "Customer{"
          + "name='" + name + '\''
          + ", age=" + age
          + '}';
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      Customer customer = (Customer) obj;
      return age == customer.age
          && Objects.equals(name, customer.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, age);
    }
  }

  private static class CustomerSummary {

    private final long total;

    public CustomerSummary(Customer... customer) {
      total = customer.length;
    }

    public CustomerSummary(CustomerSummary customerSummary, Customer customer2) {
      total = customerSummary.total + 1;
    }

    public CustomerSummary(CustomerSummary... customerSummary) {
      int temp = 0;
      for (int i = 0; i < customerSummary.length; i++) {
        temp += customerSummary[i].total;
      }
      total = temp;
    }

    public CustomerSummary(long total) {
      this.total = total;
    }

    @Override
    public String toString() {
      return "CustomerSummary{"
          + "total=" + total
          + '}';
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      CustomerSummary that = (CustomerSummary) obj;
      return total == that.total;
    }

    @Override
    public int hashCode() {
      return Objects.hash(total);
    }
  }
}
