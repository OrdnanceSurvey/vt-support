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

import java.util.Map;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import uk.os.vt.mvt.VectorTile;
import uk.os.vt.mvt.build.MvtLayerProps;

/**
 * Convert simple user data {@link Map} where the keys are {@link String} and values are
 * {@link Object}. Supports converting a specific map key to a user id. If the key to user id
 * conversion fails, the error occurs silently and the id is discarded.
 *
 * @see IUserDataConverter
 */
public final class UserDataKeyValueMapConverter implements IUserDataConverter {

  /**
   * If true, set feature id from user data.
   */
  private final boolean setId;

  /**
   * The {@link Map} key for the feature id.
   */
  private final String idKey;

  /**
   * Does not set feature id.
   */
  public UserDataKeyValueMapConverter() {
    this.setId = false;
    this.idKey = null;
  }

  /**
   * Tries to set feature id using provided user data {@link Map} key.
   *
   * @param idKey user data {@link Map} key for getting id value.
   */
  public UserDataKeyValueMapConverter(String idKey) {
    Objects.requireNonNull(idKey);
    this.setId = true;
    this.idKey = idKey;
  }

  @Override
  public void addTags(Object userData, MvtLayerProps layerProps,
                      VectorTile.Tile.Feature.Builder featureBuilder) {
    if (userData != null) {
      try {
        @SuppressWarnings("unchecked")
        final Map<String, Object> userDataMap = (Map<String, Object>) userData;

        for (Map.Entry<String, Object> e : userDataMap.entrySet()) {
          final String key = e.getKey();
          final Object value = e.getValue();

          if (key != null && value != null) {
            final int valueIndex = layerProps.addValue(value);

            if (valueIndex >= 0) {
              featureBuilder.addTags(layerProps.addKey(key));
              featureBuilder.addTags(valueIndex);
            }
          }
        }

        // Set feature id value
        if (setId) {
          final Object idValue = userDataMap.get(idKey);
          if (idValue != null) {
            if (idValue instanceof Long || idValue instanceof Integer
                || idValue instanceof Float || idValue instanceof Double
                || idValue instanceof Byte || idValue instanceof Short) {
              featureBuilder.setId((long) idValue);
            } else if (idValue instanceof String) {
              try {
                featureBuilder.setId(Long.parseLong((String) idValue));
              } catch (NumberFormatException expected) { }
            }
          }
        }

      } catch (ClassCastException exception) {
        LoggerFactory.getLogger(UserDataKeyValueMapConverter.class)
            .error(exception.getMessage(), exception);
      }
    }
  }
}
