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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A support class to simplify feature attribution.
 *
 * <p>By default, it handles numbers, booleans and strings but that behaviour can be
 * customised</p>
 */
public class AttributionClusterer {

  public static class Builder {

    Set<String> includes = new HashSet<>();
    Set<String> excludes = new HashSet<>();
    Map<String, AttributeCombiner> combiners = new HashMap<>();
    Map<Class, AttributeCombiner> combinersClazz = new HashMap<>();

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
     * Map {@link Class} to a {@link AttributeCombiner}.
     *
     * @param combiner to be applied to the given classes
     * @param clazzes  the restriction for the classes
     * @return this builder
     */
    public Builder combiner(AttributeCombiner combiner, Class... clazzes) {
      for (Class clazz : clazzes) {
        combinersClazz.put(clazz, combiner);
      }
      return this;
    }

    /**
     * Add a {@link AttributeCombiner} for the given keys.
     *
     * @param combiner to be applied for the given keys
     * @param keys     to restrict the combiner to
     * @return this combiner
     */
    public Builder addCombiner(AttributeCombiner combiner, String... keys) {
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

  private final Map<String, AttributeCombiner> combiners;
  private final Map<Class, AttributeCombiner> combinersClazz;
  private final Set<String> includes = new HashSet<>();
  private final Set<String> excludes = new HashSet<>();

  private final boolean isInclusiveRestriction;
  private final boolean isExclusiveRestriction;

  private final AttributeCombiner<Object> internalCombiner = new AttributeCombinerDefault();

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

      for (Map.Entry<String, Object> item : attr.entrySet()) {
        final String key = item.getKey();
        final Object value = item.getValue();
        Objects.requireNonNull(key, "attribute key is null");
        Objects.requireNonNull(value, "attribute value is null");

        // check if excluded
        if (isInclusiveRestriction && !includes.contains(key)) {
          continue;
        } else if (isExclusiveRestriction && excludes.contains(key)) {
          continue;
        }

        // find a combiner or use default
        if (combiners.containsKey(key)) {
          if (result.containsKey(key)) {
            Object existing = result.get(key);

            @SuppressWarnings("unchecked")
            AttributeCombiner<Object> combiner = combiners.get(key);
            result.put(key, combiner.combine(existing, value));
          } else {
            result.put(key, value);
          }
        } else if (combinersClazz.containsKey(value.getClass())) {
          Object existing = result.get(key);

          boolean isFirst = existing == null;
          if (!isFirst) {
            @SuppressWarnings("unchecked")
            AttributeCombiner<Object> combiner = combinersClazz.get(value.getClass());
            result.put(key, combiner.combine(existing, value));
          } else {
            result.put(key, value);
          }
        } else if (result.containsKey(key)) {
          Object existing = result.get(key);
          result.put(key, internalCombiner.combine(existing, value));
        } else {
          result.put(key, value);
        }
      }
    }
    return result;
  }
}
