/*
 * Copyright (C) 2017 Weather Decision Technologies
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

package uk.os.vt.mvt.build;

import java.util.LinkedHashMap;
import java.util.Objects;
import uk.os.vt.mvt.encoding.MvtValue;

/**
 * Support MVT features that must reference properties by their key and value index.
 */
public final class MvtLayerProps {
  private LinkedHashMap<String, Integer> keys;
  private LinkedHashMap<Object, Integer> vals;

  public MvtLayerProps() {
    keys = new LinkedHashMap<>();
    vals = new LinkedHashMap<>();
  }

  public Integer keyIndex(String key) {
    return keys.get(key);
  }

  public Integer valueIndex(Object value) {
    return vals.get(value);
  }

  /**
   * Add the key and return it's index code. If the key already is present, the previous
   * index code is returned and no insertion is done.
   *
   * @param key key to add
   * @return index of the key
   */
  public int addKey(String key) {
    Objects.requireNonNull(key);
    int nextIndex = keys.size();
    final Integer mapIndex = keys.putIfAbsent(key, nextIndex);
    return mapIndex == null ? nextIndex : mapIndex;
  }

  /**
   * Add the value and return it's index code. If the value already is present, the previous
   * index code is returned and no insertion is done. If {@code value} is an unsupported type
   * for encoding in a MVT, then it will not be added.
   *
   * @param value value to add
   * @return index of the value, -1 on unsupported value types
   * @see MvtValue#isValidPropValue(Object)
   */
  public int addValue(Object value) {
    Objects.requireNonNull(value);
    if (!MvtValue.isValidPropValue(value)) {
      return -1;
    }

    int nextIndex = vals.size();
    final Integer mapIndex = vals.putIfAbsent(value, nextIndex);
    return mapIndex == null ? nextIndex : mapIndex;
  }

  public Iterable<String> getKeys() {
    return keys.keySet();
  }

  public Iterable<Object> getVals() {
    return vals.keySet();
  }
}
