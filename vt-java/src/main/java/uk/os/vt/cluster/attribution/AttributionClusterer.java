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

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributionClusterer {

  private static final Logger LOG = LoggerFactory.getLogger(AttributionClusterer.class);

  public interface CustomCombiner<T> {

    /**
     * Combine two items into one of the same type.
     *
     * <p>For example, two strings "a" and "b" could be combined as "a, b".</p>
     *
     * @param existing item
     * @param item     to be combined with the existing
     * @return the combined result
     */
    T combine(T existing, T item);
  }

  public static class Builder {

    Set<String> includes = new HashSet<>();
    Set<String> excludes = new HashSet<>();
    Map<String, CustomCombiner> combiners = new HashMap<>();
    Map<Class, CustomCombiner> combinersClazz = new HashMap<>();

    /**
     * specify the keys to include.
     *
     * @param keys to include
     * @return this
     */
    public Builder includeKeys(String... keys) {
      includes.addAll(Arrays.asList(keys));
      return this;
    }

    /**
     * specify the keys to exclude.
     *
     * <p>Note: should a key be defined in both the included and excluded key then the
     * key will be excluded</p>
     *
     * @param key to exclude
     * @return this
     */
    public Builder excludeKey(String... key) {
      excludes.addAll(Arrays.asList(key));
      return this;
    }

    /**
     * Map {@link Class} to a {@link CustomCombiner}.
     *
     * @param combiner to be applied to the given classes
     * @param clazzes  the restriction for the classes
     * @return this builder
     */
    public Builder combiner(CustomCombiner combiner, Class... clazzes) {
      for (Class clazz : clazzes) {
        combinersClazz.put(clazz, combiner);
      }
      return this;
    }

    /**
     * Add a {@link CustomCombiner} for the given keys.
     *
     * @param combiner to be applied for the given keys
     * @param keys     to restrict the combiner to
     * @return this combiner
     */
    public Builder addCombiner(CustomCombiner combiner, String... keys) {
      for (String key : keys) {
        combiners.put(key, combiner);
      }
      return this;
    }

    /**
     * Build an {@link AttributionClusterer} with the defined configuration.
     *
     * @return an {@link AttributionClusterer}
     */
    public AttributionClusterer build() {
      return new AttributionClusterer(this);
    }
  }

  private final Map<String, CustomCombiner> combiners;
  private final Map<Class, CustomCombiner> combinersClazz;
  private final Set<String> includes = new HashSet<>();
  private final Set<String> excludes = new HashSet<>();

  private final boolean isInclusiveRestriction;
  private final boolean isExclusiveRestriction;

  private final CustomCombiner<Object> internalCombiner = new CustomCombiner<Object>() {
    @Override
    public Object combine(Object existingInput, Object item) {
      if (existingInput instanceof DoubleSummary) {
        if (item instanceof Number) {
          return new DoubleSummary(((DoubleSummary) existingInput),
              (((Number) item).doubleValue()));
        } else if (item instanceof DoubleSummary) {
          return new DoubleSummary(((DoubleSummary) existingInput), ((DoubleSummary) item));
        }
      }

      if (existingInput instanceof Number) {
        if (item instanceof Number) {
          return new DoubleSummary(((Number) existingInput).doubleValue(),
              ((Number) item).doubleValue());
        } else if (item instanceof DoubleSummary) {
          return new DoubleSummary(((DoubleSummary) item),
              ((Number) existingInput).doubleValue());
        }
      }

      if (existingInput instanceof StringSummary) {
        if (item instanceof String) {
          return new StringSummary((StringSummary) existingInput, (String) item);
        } else if (item instanceof StringSummary) {
          return new StringSummary((StringSummary) existingInput, (StringSummary) item);
        }
      }

      if (existingInput instanceof String) {
        if (item instanceof String) {
          return new StringSummary((String)existingInput, (String) item);
        } else if (item instanceof StringSummary) {
          return new StringSummary((StringSummary) item, (String) existingInput);
        }
      }

      if (existingInput instanceof BooleanSummary) {
        if (item instanceof Boolean) {
          return new BooleanSummary((BooleanSummary) existingInput, (boolean) item);
        } else if (item instanceof BooleanSummary) {
          return new BooleanSummary((BooleanSummary) existingInput, (BooleanSummary) item);
        }
      }

      if (existingInput instanceof Boolean) {
        if (item instanceof Boolean) {
          return new BooleanSummary((boolean)existingInput, (boolean) item);
        } else if (item instanceof BooleanSummary) {
          return new BooleanSummary((BooleanSummary) item, (boolean) existingInput);
        }
      }
      throw new IllegalArgumentException("Cannot combine: \"" + existingInput + "\" with \""
          + item + "\"");
    }
  };

  private AttributionClusterer(Builder builder) {
    includes.addAll(builder.includes);
    // excludes win: if in included and excluded then excluded will win
    includes.removeAll(excludes);
    excludes.addAll(builder.excludes);

    isInclusiveRestriction = includes.size() > 0;
    isExclusiveRestriction = excludes.size() > 0;
    combiners = new HashMap<>(builder.combiners);
    combinersClazz = new HashMap<>(builder.combinersClazz);
  }

  // TODO reconsider deserialization process - should we restore MaxMinMeanSum entry there or here?

  /**
   * Combine multiple attributes.
   *
   * @param attrs to be combined
   * @return a single set of attributes (key value pairs)
   */
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public final Map<String, Object> combine(Map<String, Object>... attrs) {
    // preserve insertion order
    Map<String, Object> result = new LinkedHashMap<>();

    for (Map<String, Object> attr : attrs) {
      Objects.requireNonNull(attr);
      attr = deserialize(attr);

      for (Map.Entry<String, Object> item : attr.entrySet()) {
        final String key = item.getKey();
        final Object value = item.getValue();
        Objects.requireNonNull(key, "attribute key is null");
        Objects.requireNonNull(value, "attribute value is null");

        if (isInclusiveRestriction && !includes.contains(key)) {
          continue;
        } else if (isExclusiveRestriction && excludes.contains(key)) {
          continue;
        }

        if (combiners.containsKey(key)) {
          if (result.containsKey(key)) {
            Object existing = result.get(key);

            @SuppressWarnings("unchecked")
            CustomCombiner<Object> combiner = combiners.get(key);
            result.put(key, combiner.combine(existing, value));
          } else {
            result.put(key, value);
          }
          continue;
        }

        if (combinersClazz.containsKey(value.getClass())) {
          Object existing = result.get(key);

          boolean isFirst = existing == null;
          if (!isFirst) {
            @SuppressWarnings("unchecked")
            CustomCombiner<Object> combiner = combinersClazz.get(value.getClass());
            result.put(key, combiner.combine(existing, value));
          } else {
            result.put(key, value);
          }
          continue;
        }

        if (result.containsKey(key)) {
          Object existing = result.get(key);
          result.put(key, internalCombiner.combine(existing, value));
        } else {
          result.put(key, value);
        }
      }
    }
    return result;
  }

  private static boolean containsAll(Map<String, Object> map, String... keys) {
    for (String key : keys) {
      if (!map.containsKey(key)) {
        return false;
      }
    }
    return true;
  }

  private static long getLongFrom(Object object) {
    try {
      return (long) Double.parseDouble(object.toString());
    } catch (NumberFormatException exception) {
      exception.printStackTrace();
      return Long.MIN_VALUE;
    }
  }

  private static double getDoubleFrom(Object object) {
    if (object == null) {
      // TODO consider throwing an exception?
      System.err.println("Error - cannot get double from null!");
      return Double.MIN_VALUE;
    }
    try {
      return Double.parseDouble(object.toString());
    } catch (NumberFormatException exception) {
      return Double.MIN_VALUE;
    }
  }

  private static String getStringFrom(Object object, String defaultValue) {
    if (object == null) {
      return defaultValue;
    }
    return String.valueOf(object);
  }

  /**
   * Returns a Map containing {@link DoubleSummary} values.
   *
   * <p>Upon serialisation, {@link DoubleSummary} attribute values are are expanded into
   * attribute max, min, sum, total values.</p>
   *
   * @param values the raw attributes
   * @return the attributes including any {@link DoubleSummary} values
   */
  public static Map<String, Object> deserialize(Map<String, Object> values) {
    Map<String, Object> result = new LinkedHashMap<>();

    Pattern pattern = Pattern.compile("^(.*):(.*)$");
    for (Map.Entry<String, Object> item : values.entrySet()) {
      final String key = item.getKey();
      final Object value = item.getValue();
      Matcher matcher = pattern.matcher(key);
      if (matcher.matches()) {
        String possibleAttribute = matcher.group(1);

        // duck typing
        boolean isNewMatch = !result.containsKey(possibleAttribute);
        boolean isDs = containsAll(values, format(possibleAttribute, "max"),
            format(possibleAttribute, "min"), format(possibleAttribute, "sum"),
            format(possibleAttribute, "total")) && isNewMatch;
        boolean isSs = containsAll(values, format(possibleAttribute, "subset"),
            format(possibleAttribute, "total")) && isNewMatch;
        boolean isBs = containsAll(values, format(possibleAttribute, "trues"),
            format(possibleAttribute, "falses"), format(possibleAttribute, "total"))
            && isNewMatch;

        if (isDs) {
          double max = getDoubleFrom(values.get(format(possibleAttribute, "max")));
          double min = getDoubleFrom(values.get(format(possibleAttribute, "min")));
          double sum = getDoubleFrom(values.get(format(possibleAttribute, "sum")));
          long samples = getLongFrom(values.get(format(possibleAttribute, "total")));

          if (!isDoubleError(max, min, sum) && !isLongError(samples)) {
            DoubleSummary doubleSummary = new DoubleSummary(max, min, samples, sum);
            result.put(possibleAttribute, doubleSummary);
          }
        } else if (isSs) {
          String rawString = getStringFrom(values.get(format(possibleAttribute, "subset")), null);
          boolean isError = rawString == null;
          if (isError) {
            LOG.error("problem with StringSummary - null item! " + values);
            continue;
          }

          SortedSet<String> subset = deserializeStringSet(rawString, null);
          isError = subset == null;
          if (isError) {
            LOG.error("problem deserializing StringSummary! " + rawString);
            continue;
          }

          long samples = getLongFrom(values.get(format(possibleAttribute, "total")));
          isError = isLongError(samples);
          if (isError) {
            LOG.error("problem with StringSummary - ignoring! " + values);
            continue;
          }

          StringSummary stringSummary = new StringSummary(subset, samples);
          result.put(possibleAttribute, stringSummary);
        } else if (isBs) {
          long trues = getLongFrom(values.get(format(possibleAttribute, "trues")));
          long falses = getLongFrom(values.get(format(possibleAttribute, "falses")));
          long samples = getLongFrom(values.get(format(possibleAttribute, "total")));

          boolean isError = isLongError(trues, falses, samples);
          if (!isError) {
            BooleanSummary booleanSummary = new BooleanSummary(trues, falses, samples);
            result.put(possibleAttribute, booleanSummary);
          } else {
            LOG.error("problem with BooleanSummary - ignoring! " + values);
          }
        } else if (isNewMatch) {
          result.put(key, value);
        }
      } else {
        result.put(key, value);
      }
    }
    return result;
  }

  private static boolean isDoubleError(double... values) {
    for (int i = 0; i < values.length; i++) {
      if (Double.compare(values[i], Double.MIN_VALUE) == 0) {
        return true;
      }
    }
    return false;
  }

  private static boolean isLongError(long... values) {
    for (int i = 0; i < values.length; i++) {
      if (values[i] == Long.MIN_VALUE) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a Map without complex attribute objects such as {@link DoubleSummary}.
   *
   * <p>Upon serialisation, {@link DoubleSummary} attribute values are expanded into attribute
   * max, min, sum, total values.</p>
   *
   * @param values the geometry attributes
   * @return the attributes including any {@link DoubleSummary} values
   */
  public static Map<String, Object> serialize(Map<String, Object> values) {
    Map<String, Object> result = new LinkedHashMap<>();

    for (Map.Entry<String, Object> item : values.entrySet()) {
      final String key = item.getKey();
      final Object value = item.getValue();

      if (value instanceof DoubleSummary) {
        DoubleSummary doubleSummary = (DoubleSummary) value;
        result.put(format(key, "max"), doubleSummary.getMax());
        result.put(format(key, "min"), doubleSummary.getMin());
        result.put(format(key, "sum"), doubleSummary.getSum());
        result.put(format(key, "total"), doubleSummary.getTotalSamples());
      } else if (value instanceof StringSummary) {
        StringSummary stringSummary = (StringSummary) value;
        result.put(format(key, "subset"), serializeStringSet(stringSummary.getSample()));
        result.put(format(key, "total"), stringSummary.getTotalSamples());
      } else if (value instanceof BooleanSummary) {
        BooleanSummary booleanSummary = (BooleanSummary) value;
        result.put(format(key, "falses"), booleanSummary.getFalses());
        result.put(format(key, "trues"), booleanSummary.getTrues());
        result.put(format(key, "total"), booleanSummary.getSamples());
      } else {
        result.put(key, values.get(key));
      }
    }
    return result;
  }

  private static String format(String input, String type) {
    return String.format("%s:%s", input, type);
  }

  private static String serializeStringSet(SortedSet<String> sample) {
    StringBuilder sb = new StringBuilder(sample.size());
    Iterator<String> iterator = sample.iterator();
    if (iterator.hasNext()) {
      String first = iterator.next();
      sb.append(CSVFormat.RFC4180.format(first));
    }
    while (iterator.hasNext()) {
      String item = iterator.next();
      sb.append(",").append(CSVFormat.RFC4180.format(item));
    }
    return sb.toString();
  }

  private static SortedSet<String> deserializeStringSet(String string,
                                                        SortedSet<String> defaultValue) {
    try {
      SortedSet<String> sample = new TreeSet<>();
      CSVParser items = CSVFormat.RFC4180.parse(new StringReader(string));
      List<CSVRecord> records = items.getRecords();
      for (CSVRecord record : records) {
        for (String field : record) {
          sample.add(field);
        }
      }
      return sample;
    } catch (IOException ex) {
      return defaultValue;
    }
  }
}
