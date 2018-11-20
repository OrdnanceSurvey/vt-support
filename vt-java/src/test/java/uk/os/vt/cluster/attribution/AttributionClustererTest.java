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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import org.junit.Test;
import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

public class AttributionClustererTest {

  @Test
  public void customCombinerForClass() {
    Map<String, Object> attr = new HashMap<>();
    attr.put("recommended", new Customer("Ben", 1951));

    Map<String, Object> attr2 = new HashMap<>();
    attr2.put("recommended", new Customer("Jerry", 1951));

    AttributionClusterer attributionClusterer = new AttributionClusterer.Builder()
        .combiner((existing, item) -> {
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
        }, Customer.class, CustomerSummary.class)
        .build();

    Map<String, Object> result = attributionClusterer.combine(attr, attr2);
    CustomerSummary expected = new CustomerSummary(2);
    assertEquals(expected, result.get("recommended"));
  }

  @Test
  public void customCombinerForKeys() {
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

    AttributeCombiner colonCombiner = (existing, item) -> {
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
  public void customCombinerExcludesOverride() {
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
