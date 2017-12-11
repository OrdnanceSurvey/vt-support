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

package uk.os.vt.mvt.adapt.jts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import uk.os.vt.mvt.VectorTile;
import uk.os.vt.mvt.encoding.MvtValue;

/**
 * Convert MVT tags list to a {@link Map} of {@link String} to {@link Object}. Tags indices that are
 * out of range of the key or value list are ignored.
 *
 * @see ITagConverter
 */
public final class TagKeyValueMapConverter implements ITagConverter {

  /**
   * If true, return null user data when tags are empty.
   */
  private final boolean nullIfEmpty;

  /**
   * If true, add id to user data object.
   */
  private final boolean addId;

  /**
   * The {@link Map} key for the feature id.
   */
  private final String idKey;

  /**
   * Always created user data object, even with empty tags. Ignore feature ids.
   */
  public TagKeyValueMapConverter() {
    this(false);
  }

  /**
   * Ignore feature ids.
   *
   * @param nullIfEmpty if true, return null user data when tags are empty
   */
  public TagKeyValueMapConverter(boolean nullIfEmpty) {
    this.nullIfEmpty = nullIfEmpty;
    this.addId = false;
    this.idKey = null;
  }

  /**
   * Store feature ids using idKey. Id value may be null if not present.
   *
   * @param nullIfEmpty if true, return null user data when tags are empty
   * @param idKey       key name to use for feature id value
   */
  public TagKeyValueMapConverter(boolean nullIfEmpty, String idKey) {
    Objects.requireNonNull(idKey);

    this.nullIfEmpty = nullIfEmpty;
    this.addId = true;
    this.idKey = idKey;
  }

  @Override
  public Object toUserData(Long id, List<Integer> tags, List<String> keysList,
                           List<VectorTile.Tile.Value> valuesList) {

    // Guard: empty
    if (nullIfEmpty && tags.size() < 1 && (!addId || id == null)) {
      return null;
    }


    final Map<String, Object> userData = new LinkedHashMap<>(((tags.size() + 1) / 2));

    // Add feature properties
    int keyIndex;
    int valIndex;
    boolean valid;

    for (int i = 0; i < tags.size() - 1; i += 2) {
      keyIndex = tags.get(i);
      valIndex = tags.get(i + 1);

      valid = keyIndex >= 0 && keyIndex < keysList.size()
          && valIndex >= 0 && valIndex < valuesList.size();

      if (valid) {
        userData.put(keysList.get(keyIndex), MvtValue.toObject(valuesList.get(valIndex)));
      }
    }

    // Add ID, value may be null
    if (addId) {
      userData.put(idKey, id);
    }

    return userData;
  }
}
