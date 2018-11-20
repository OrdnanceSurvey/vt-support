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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import org.apache.commons.csv.CSVFormat;
import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

/**
 * Simple Attribution Serializer.
 *
 * <p>
 *   This serializer is a reasonable compromise to store aggregate data in a way that appears
 *   acceptable to legacy GIS systems, which typically can only store primitive data types alongside
 *   features.
 *   Note, however, that attributes could easily become mingled without appropriate consideration
 *   for the <&lt;attribute&gt;>:<&lt;aggregate_dimension&gt;> serialization structure.
 * </p>
 */
public class AttributionSerializerImpl {

  private AttributionSerializerImpl() {}

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
}
