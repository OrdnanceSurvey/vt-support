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

package uk.os.vt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonUtil {

  private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

  private JsonUtil() {}

  /**
   * Get a string ignoring errors.
   *
   * @param index index into string
   * @param jsonArray json array
   * @param defaultValue default value
   * @return the string
   */
  public static String getStringIgnoreErrors(int index, JSONArray jsonArray, String defaultValue) {
    if (jsonArray.length() > index) {
      try {
        return jsonArray.getString(index);
      } catch (final JSONException ex) {
        LOG.error("problem getting attribute", ex);
      }
    }
    return defaultValue;
  }

  protected static String getStringIgnoreErrors(String attribute, JSONObject source) {
    if (source.has(attribute)) {
      try {
        return source.getString(attribute);
      } catch (final JSONException ex) {
        LOG.error("problem getting attribute", ex);
      }
    }
    return "";
  }

  protected static String getStringIgnoreErrors(String attribute, JSONObject source,
      String defaultValue) {
    if (source.has(attribute)) {
      try {
        return source.getString(attribute);
      } catch (final JSONException ex) {
        LOG.error("problem getting int", ex);
      }
    }
    return defaultValue;
  }

  protected static void putIgnoreErrors(JSONObject jsonObject, String attribute, double[] value) {
    try {
      jsonObject.put(attribute, new JSONArray(value));
    } catch (final JSONException ex) {
      LOG.error("cannot add JSON attribute(s)", ex);
    }
  }

  protected static void putIgnoreErrors(JSONObject jsonObject, String attribute, String value) {
    try {
      jsonObject.put(attribute, value);
    } catch (final JSONException ex) {
      LOG.error("cannot add JSON attribute(s)", ex);
    }
  }

  protected static void putIgnoreErrors(JSONObject jsonObject, String attribute, int value) {
    try {
      jsonObject.put(attribute, value);
    } catch (final JSONException ex) {
      LOG.error("cannot add JSON attribute(s)", ex);
    }
  }

  protected static void putIgnoreErrors(JSONObject jsonObject, String attribute, double value) {
    try {
      jsonObject.put(attribute, value);
    } catch (final JSONException ex) {
      LOG.error("cannot add JSON attribute(s)", ex);
    }
  }

  protected static int getIntegerIgnoreErrors(String attribute, JSONObject source,
      int defaultValue) {
    if (source.has(attribute)) {
      try {
        return source.getInt(attribute);
      } catch (final JSONException ex) {
        LOG.error("problem getting attribute", ex);
      }
    }
    return defaultValue;
  }

  protected static double[] getDoubleArrayIgnoreErrors(String attribute, JSONObject source,
      double[] defaultValue) {
    if (source.has(attribute)) {
      try {
        final JSONArray array = source.getJSONArray(attribute);
        final double[] item = new double[array.length()];

        for (int i = 0; i < array.length(); i++) {
          item[i] = array.getDouble(i);
        }
        return item;
      } catch (final JSONException ex) {
        LOG.error("problem getting int", ex);
      }
    }
    return defaultValue;
  }
}
