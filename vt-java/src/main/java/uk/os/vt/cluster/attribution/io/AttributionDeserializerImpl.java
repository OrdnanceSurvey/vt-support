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

import static uk.os.vt.cluster.attribution.io.Util.format;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

/**
 * Basic deserializer for {@link BooleanSummary}, {@link DoubleSummary} and {@link StringSummary}
 * types.
 */
public class AttributionDeserializerImpl {

  private static final Logger LOG = LoggerFactory.getLogger(AttributionDeserializerImpl.class);

  private AttributionDeserializerImpl() {}

  private static void addCompletedKeys(Set<String> completedKeys, String... keys) {
    completedKeys.addAll(Arrays.asList(keys));
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

    Set<String> completedKeys = new HashSet<>();

    Pattern pattern = Pattern.compile("^(.*):(.*)$");
    for (Map.Entry<String, Object> item : values.entrySet()) {
      final String key = item.getKey();
      final Object value = item.getValue();

      if (completedKeys.contains(key)) {
        continue;
      }

      Matcher matcher = pattern.matcher(key);
      if (matcher.matches()) {
        String possibleAttribute = matcher.group(1);

        // duck typing
        boolean isDs = containsAll(values, format(possibleAttribute, "max"),
            format(possibleAttribute, "min"), format(possibleAttribute, "sum"),
            format(possibleAttribute, "total"));
        boolean isSs = containsAll(values, format(possibleAttribute, "subset"),
            format(possibleAttribute, "total"));
        boolean isBs = containsAll(values, format(possibleAttribute, "trues"),
            format(possibleAttribute, "falses"), format(possibleAttribute, "total"));

        if (isDs) {
          double max = getDoubleFrom(values.get(format(possibleAttribute, "max")));
          double min = getDoubleFrom(values.get(format(possibleAttribute, "min")));
          double sum = getDoubleFrom(values.get(format(possibleAttribute, "sum")));
          long samples = getLongFrom(values.get(format(possibleAttribute, "total")));

          if (!isDoubleError(max, min, sum) && !isLongError(samples)) {
            // add keys as to avoid subsequent processing
            addCompletedKeys(completedKeys,
                format(possibleAttribute, "max"),
                format(possibleAttribute, "min"),
                format(possibleAttribute, "sum"),
                format(possibleAttribute, "total"));

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
          // add keys as to avoid subsequent processing
          addCompletedKeys(completedKeys,
              format(possibleAttribute, "subset"),
              format(possibleAttribute, "total"));
          StringSummary stringSummary = new StringSummary(subset, samples);
          result.put(possibleAttribute, stringSummary);
        } else if (isBs) {
          long trues = getLongFrom(values.get(format(possibleAttribute, "trues")));
          long falses = getLongFrom(values.get(format(possibleAttribute, "falses")));
          long samples = getLongFrom(values.get(format(possibleAttribute, "total")));

          boolean isError = isLongError(trues, falses, samples);
          if (!isError) {
            // add keys as to avoid subsequent processing
            addCompletedKeys(completedKeys,
                format(possibleAttribute, "trues"),
                format(possibleAttribute, "falses"),
                format(possibleAttribute, "total"));
            BooleanSummary booleanSummary = new BooleanSummary(trues, falses, samples);
            result.put(possibleAttribute, booleanSummary);
          } else {
            LOG.error("problem with BooleanSummary - ignoring! " + values);
          }
        } else {
          // add keys (for completeness)
          addCompletedKeys(completedKeys, key);
          result.put(key, value);
        }
      } else {
        addCompletedKeys(completedKeys, key);
        result.put(key, value);
      }
    }
    return result;
  }

  private static long getLongFrom(Object object) {
    if (object == null) {
      return Long.MIN_VALUE;
    }
    try {
      return (long) Double.parseDouble(object.toString());
    } catch (NumberFormatException ignore) {
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
    } catch (NumberFormatException ignore) {
      return Double.MIN_VALUE;
    }
  }

  private static String getStringFrom(Object object, String defaultValue) {
    if (object == null) {
      return defaultValue;
    }
    return String.valueOf(object);
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

  private static boolean containsAll(Map<String, Object> map, String... keys) {
    for (String key : keys) {
      if (!map.containsKey(key)) {
        return false;
      }
    }
    return true;
  }
}
