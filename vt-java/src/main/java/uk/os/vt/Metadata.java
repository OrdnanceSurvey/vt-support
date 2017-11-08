/**
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

import static uk.os.vt.JsonUtil.getDoubleArrayIgnoreErrors;
import static uk.os.vt.JsonUtil.getIntegerIgnoreErrors;
import static uk.os.vt.JsonUtil.getJsonArrayAsListIgnoreErrors;
import static uk.os.vt.JsonUtil.getMap;
import static uk.os.vt.JsonUtil.getStringIgnoreErrors;
import static uk.os.vt.JsonUtil.putIgnoreErrors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metadata {

  private static final int WEST = 0;
  private static final int SOUTH = 1;
  private static final int EAST = 2;
  private static final int NORTH = 3;

  private static final int X = 0;
  private static final int Y = 1;
  private static final int Z = 2;

  private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

  private final String name;
  private final String description;
  private final String attribution;
  private final int minZoom;
  private final int maxZoom;
  private final double[] bounds;
  private final double[] center;

  /*
   * tl;dr:
   * > tilejson        - the style version number.  THIS IS REQUIRED
   * <p>
   * > name            - the name of the tileset. It SHOULD NOT be interpreted as HTML
   * default: null AND optional
   * <p>
   * > description     - the description of the tileset.  It SHOULD NOT be interpreted as HTML
   * default: null
   * <p>
   * > version         - x.x.x style version number.  Minor value MUST when tile(s) change.
   * default: 1.0.0 BUT optional
   * <p>
   * > attribution     - the attribution displayed to the user.  HTML or literal text.
   * default: null AND optional
   * <p>
   * > minzoom         - the minimum zoom level
   * default: 0 BUT optional
   * <p>
   * > maxzoom         - the maximum zoom level
   * default: 22 BUT optional
   * <p>
   * > bounds          - the max extent of map tiles.  Bounds MUST define the area covered by all
   *                     zoom levels.
   * default: -180, -90, 180, 90 BUT optional
   * <p>
   * > center          - longitude, latitude (WGS84), zoom level.  Furthermore:
   * * the center must be within the bounds.
   * * the zoom level must be between the minzoom and maxzoom.
   * default: null AND optional
   * <p>
   * Source: https://github.com/mapbox/tilejson-spec/tree/master/2.1.0
   */
  private final JSONObject tileJson;

  /**
   * Create a new Metadata object from a tile json.
   *
   * @param tileJson the source of truth for the Metadata object
   */
  private Metadata(JSONObject tileJson) {
    this.tileJson = tileJson;
    name = getStringIgnoreErrors("name", tileJson, null);
    description = getStringIgnoreErrors("description", tileJson, null);
    minZoom = getIntegerIgnoreErrors("minzoom", tileJson, 0);
    maxZoom = getIntegerIgnoreErrors("maxzoom", tileJson, 22);
    bounds = getDoubleArrayIgnoreErrors("bounds", tileJson, new double[] {-180, -90, 180, 90});
    attribution = getStringIgnoreErrors("attribution", tileJson, null);
    center = getDoubleArrayIgnoreErrors("center", tileJson, null);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getAttribution() {
    return attribution;
  }

  public int getMinZoom() {
    return minZoom;
  }

  public int getMaxZoom() {
    return maxZoom;
  }

  public double getCenterLat(double defaultValue) {
    return center != null ? center[Y] : defaultValue;
  }

  public double getCenterLon(double defaultValue) {
    return center != null ? center[X] : defaultValue;
  }

  public double getCenterZoom(double defaultValue) {
    return center != null ? center[Z] : defaultValue;
  }

  /**
   * Get the bounds.
   *
   * @return west, south, east, north
   */
  public double[] getBounds() {
    return bounds.clone();
  }

  /**
   * Get the center.
   *
   * @return longitude (x), latitude (y), zoom
   */
  public double[] getCenter() {
    return center.clone();
  }

  public double getBoundsWest() {
    return bounds[WEST];
  }

  public double getBoundsNorth() {
    return bounds[NORTH];
  }

  public double getBoundsEast() {
    return bounds[EAST];
  }

  public double getBoundsSouth() {
    return bounds[SOUTH];
  }

  /**
   * Get the JSON for a tile.
   *
   * @return the JSON
   */
  public JSONObject getTileJson() {
    try {
      return new JSONObject(tileJson.toString());
    } catch (final JSONException ex) {
      LOG.error("problem creating new JSONObject", ex);
      return new JSONObject();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final Metadata metadata = (Metadata) obj;

    if (minZoom != metadata.minZoom) {
      return false;
    }
    if (maxZoom != metadata.maxZoom) {
      return false;
    }
    if (name != null ? !name.equals(metadata.name) : metadata.name != null) {
      return false;
    }
    if (description != null ? !description.equals(metadata.description)
        : metadata.description != null) {
      return false;
    }
    if (attribution != null ? !attribution.equals(metadata.attribution)
        : metadata.attribution != null) {
      return false;
    }
    if (!Arrays.equals(bounds, metadata.bounds)) {
      return false;
    }
    if (!Arrays.equals(center, metadata.center)) {
      return false;
    }
    return tileJson != null ? tileJson.equals(metadata.tileJson) : metadata.tileJson == null;

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (attribution != null ? attribution.hashCode() : 0);
    result = 31 * result + minZoom;
    result = 31 * result + maxZoom;
    result = 31 * result + Arrays.hashCode(bounds);
    result = 31 * result + Arrays.hashCode(center);
    result = 31 * result + (tileJson != null ? tileJson.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Metadata{" + (name == null ? "" : "name='" + name + '\'')
        + (description == null ? "" : ", description='" + description + '\'')
        + (attribution == null ? "" : ", attribution='" + attribution + '\'') + ", minZoom="
        + minZoom + ", maxZoom=" + maxZoom + ", bounds=" + Arrays.toString(bounds)
        + (center == null ? "" : ", center=" + Arrays.toString(center)) + ", tileJson=" + tileJson
        + '}';
  }

  /**
   * Provides a list of vector tile layers.
   *
   * @return all vector layers as defined by metadata
   */
  public List<Layer> getLayers() {
    if (!tileJson.has("vector_layers")) {
      return new ArrayList<>();
    }

    List<Layer> result = new ArrayList<>();
    JSONArray layers = tileJson.optJSONArray("vector_layers");
    for (int i = 0; i < layers.length(); i++) {
      JSONObject raw = layers.optJSONObject(i);
      result.add(new Layer(raw));
    }
    return result;
  }

  /**
   * TODO: consider "feature_tags" and "geometry_type" properties - I have not seen that on Mapbox
   * output See: https://github.com/mapbox/tilejson-spec/issues/14#issuecomment-251776565
   */
  public static final class Layer {

    private final String id;
    private final String description;
    private final int minZoom;
    private final int maxZoom;
    private final List<Attribute> attributes;

    public static final class Builder {

      private JSONObject json = new JSONObject();
      private boolean hasOrderedFields;

      public Builder() {}

      /**
       * The existing Layer Metadata to base this on.
       *
       * @param existing metadata
       */
      public Builder(Metadata.Layer existing) {
        json = existing.getJson();
      }

      /**
       * Add a field.
       *
       * @param key the key
       * @param desc the description
       * @return this builder
       */
      public Builder addField(String key, String desc) {
        try {
          if (!json.has("fields")) {
            json.put("fields", new JSONObject());
          }

          final JSONObject fields = json.getJSONObject("fields");
          fields.put(key, desc);

          if (hasOrderedFields) {
            if (!json.has("fields_order")) {
              json.put("fields_order", new JSONArray());
            }

            JSONArray fieldsOrder = json.getJSONArray("fields_order");
            int fieldPosition = getFieldPosition(key, fieldsOrder);

            boolean hasFieldPosition = fieldPosition != -1;
            if (hasFieldPosition) {
              JSONArray newFieldsOrder = new JSONArray();
              for (int i = 0; i < fieldsOrder.length(); i++) {
                if (fieldPosition == i) {
                  continue;
                }
                newFieldsOrder.put(fieldsOrder.get(i));
              }
              json.put("fields_order", newFieldsOrder);
              fieldsOrder = json.getJSONArray("fields_order");
            }
            fieldsOrder.put(key);
          }
        } catch (final JSONException ex) {
          LOG.error("problem adding field", ex);
        }
        return this;
      }

      /**
       * Add one or more fields.
       *
       * @param attributes the attribute values
       * @return this builder
       */
      public Builder addField(Attribute... attributes) {
        for (Attribute attribute : attributes) {
          addField(attribute.getName(), attribute.getDescription());
        }
        return this;
      }

      public Builder preserveFieldOrder(boolean isOrder) {
        hasOrderedFields = isOrder;
        return this;
      }

      public Builder setDescription(String value) {
        JsonUtil.putIgnoreErrors(json, "description", value);
        return this;
      }

      public Builder setId(String value) {
        JsonUtil.putIgnoreErrors(json, "id", value);
        return this;
      }

      public Builder setJson(JSONObject value) {
        json = value;
        return this;
      }

      public Builder setMaxZoom(int value) {
        JsonUtil.putIgnoreErrors(json, "maxzoom", value);
        return this;
      }

      public Builder setMinZoom(int value) {
        JsonUtil.putIgnoreErrors(json, "minzoom", value);
        return this;
      }

      /**
       * Set the source.
       *
       * @param source the dataset ID. For example, "os.os-terrain-50-v2"
       * @param sourceName the descriptive source name. For example, "OS Terrain 50"
       * @return this builder
       */
      public Builder setSource(String source, String sourceName) {
        JsonUtil.putIgnoreErrors(json, "source", source);
        JsonUtil.putIgnoreErrors(json, "source_name", sourceName);
        return this;
      }

      /**
       * Create the Layer from any given configuration.
       *
       * @return the built layer
       */
      public Layer build() {
        return new Layer(json);
      }

      /**
       *
       * @param value the item under test
       * @param fieldsOrder the field array, which signifies order.
       * @return the position of the value in the array or -1 if not found.
       * @throws JSONException thrown if fieldsOrder has anything other than String descriptions.
       */
      private int getFieldPosition(String value, JSONArray fieldsOrder) throws JSONException {
        for (int i = 0; i < fieldsOrder.length(); i++) {
          String thisValue = fieldsOrder.getString(i);

          if (value.equals(thisValue)) {
            return i;
          }
        }
        return -1;
      }
    }

    private final JSONObject json;

    private Layer(JSONObject json) {
      this.json = json;
      id = getStringIgnoreErrors("id", json, null);
      description = getStringIgnoreErrors("description", json, null);
      minZoom = getIntegerIgnoreErrors("minzoom", json, 0);
      maxZoom = getIntegerIgnoreErrors("maxzoom", json, 22);
      attributes = getAttributesFromJson(json);
    }

    public String getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }

    public int getMinZoom() {
      return minZoom;
    }

    public int getMaxZoom() {
      return maxZoom;
    }

    public List<Attribute> getAttributes() {
      return attributes;
    }

    /**
     * Get the JSONObject for a tile JSON.
     *
     * @return the JSONObject
     */
    public JSONObject getJson() {
      try {
        return new JSONObject(json.toString());
      } catch (final JSONException ex) {
        LOG.error("problem creating new JSONObject", ex);
        return new JSONObject();
      }
    }

    private static List<Attribute> getAttributesFromJson(JSONObject json) {
      List<Attribute> attributes = new ArrayList<>();

      Map<String, String> map = getOrderedAttributeDescriptionMap(json);
      for (Map.Entry<String, String> entry : map.entrySet()) {
        Attribute attribute = new Attribute.Builder()
            .setName(entry.getKey())
            .setDescription(entry.getValue())
            .build();
        attributes.add(attribute);
      }
      return Collections.unmodifiableList(attributes);
    }

    private static Map<String, String> getOrderedAttributeDescriptionMap(JSONObject json) {
      Map<String, String> fieldsUnordered = getMap("fields", json,
          new LinkedHashMap<String, String>());
      List<String> fieldsOrder = getJsonArrayAsListIgnoreErrors("fields_order", json,
          new ArrayList<String>());
      Map<String, String> fieldsOrdered = new LinkedHashMap<>();
      for (String field : fieldsOrder) {
        String value = fieldsUnordered.remove(field);
        fieldsOrdered.put(field, value);
      }
      // add remaining
      fieldsOrdered.putAll(fieldsUnordered);
      return fieldsOrdered;
    }

    public static final class Attribute {
      private final String name;
      private final String description;

      private Attribute(String name, String description) {
        this.name = name;
        this.description = description;
      }

      public String getName() {
        return name;
      }

      public String getDescription() {
        return description;
      }

      @Override
      public boolean equals(Object object) {
        if (this == object) {
          return true;
        }

        if (object == null || getClass() != object.getClass()) {
          return false;
        }

        Attribute attribute = (Attribute) object;

        if (!name.equals(attribute.name)) {
          return false;
        }
        return description.equals(attribute.description);
      }

      @Override
      public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + description.hashCode();
        return result;
      }

      @Override
      public String toString() {
        return "Attribute{"
            + "name='" + name + '\''
            + ", description='" + description + '\''
            + '}';
      }

      public static final class Builder {

        private String name = "";
        private String description = "";

        public Builder() {}

        public Builder setName(String name) {
          this.name = name;
          return this;
        }

        public Builder setDescription(String description) {
          this.description = description;
          return this;
        }

        /**
         * Build an Attribute.
         *
         * @return the built attribute
         */
        public Attribute build() {
          if (name == null) {
            throw new IllegalArgumentException("attribute name must be defined");
          }
          if (description == null) {
            description = "";
          }
          return new Attribute(name, description);
        }
      }
    }
  }

  public static final class Builder {

    // These values are default according to the TileJSON 2.1.0
    // See: https://github.com/mapbox/tilejson-spec/tree/master/2.1.0
    private JSONObject tileJson = getDefaultTileJson();

    private static JSONObject getDefaultTileJson() {
      final JSONObject jsonObject = new JSONObject();
      putIgnoreErrors(jsonObject, "tilejson", "2.1.0");
      return jsonObject;
    }

    public Builder() {}

    /**
     * The existing Metadata to base this on.
     *
     * @param existing metadata
     */
    public Builder(Metadata existing) {
      tileJson = existing.getTileJson();
    }

    // TODO think about!!!

    /**
     * Append the metadata.
     *
     * @param metadata the metadata
     * @return this builder
     */
    public Builder appendMetadata(Metadata metadata) {
      final boolean isTileJson = metadata.getTileJson() != null;
      if (isTileJson) {
        final JSONObject json = metadata.getTileJson();
        try {
          final JSONArray layers = json.getJSONArray("vector_layers");
          for (int i = 0; i < layers.length(); i++) {
            final JSONObject layer = layers.getJSONObject(i);
            addLayer(new Layer.Builder().setJson(layer).build());
          }
        } catch (final JSONException ex) {
          ex.printStackTrace();
        }

        // tileJson = metadata.getTileJson();
      }
      return this;
    }

    /**
     * Copy the Metadata object.
     *
     * @param metadata the metadata
     * @return this builder
     */
    public Builder copyMetadata(Metadata metadata) {
      final boolean isTileJson = metadata.getTileJson() != null;
      if (isTileJson) {
        tileJson = metadata.getTileJson();
      }
      return this;
    }

    /**
     * Get a string from a JSON source, ignoring errors.
     *
     * @param attribute e.g. description
     * @param source the data source for the above attribute
     * @return the string
     */
    private static String getStringIgnoreErrors(String attribute, JSONObject source) {
      try {
        return source.getString(attribute);
      } catch (final JSONException ex) {
        ex.printStackTrace();
      }
      return "";
    }

    /**
     * Set a JSON value.
     *
     * @param value the value
     * @return the builder
     */
    public Builder setTileJson(JSONObject value) {
      tileJson = value;
      return this;
    }

    /**
     * Set the tile JSON.
     *
     * @param value the tile JOSN
     * @return this builder
     */
    public Builder setTileJson(String value) {
      try {
        tileJson = new JSONObject(value);
      } catch (final JSONException ex) {
        LOG.error("problem adding JSON", ex);
      }
      return this;
    }

    /**
     * Set a JSON key and value object.
     *
     * @param key the key
     * @param value the value
     * @return this builder
     */
    public Builder setJson(String key, Object value) {
      try {
        tileJson.put(key, value);
      } catch (final JSONException ex) {
        LOG.error("problem adding JSON", ex);
      }
      return this;
    }

    /**
     * Set a JSON key and value array.
     *
     * @param key the key
     * @param value the value
     * @return this builder
     */
    public Builder setJson(String key, JSONArray value) {
      try {
        tileJson.put(key, value);
      } catch (final JSONException ex) {
        LOG.error("problem adding JSON", ex);
      }
      return this;
    }

    /**
     * Set a JSON key and value object.
     *
     * @param key the key
     * @param value the value
     * @return this builder
     */
    public Builder setJson(String key, JSONObject value) {
      try {
        tileJson.put(key, value);
      } catch (final JSONException ex) {
        LOG.error("problem adding JSON", ex);
      }
      return this;
    }

    /**
     * Set a JSON key and value string.
     *
     * @param key the key
     * @param value the value
     * @return this builder
     */
    public Builder setString(String key, String value) {
      putIgnoreErrors(tileJson, key, value);
      return this;
    }

    /**
     * Set the name.
     *
     * @param value a name describing the tileset. The name can contain any legal character.
     *        Implementations SHOULD NOT interpret the name as HTML!
     * @return this builder
     */
    public Builder setName(String value) {
      putIgnoreErrors(tileJson, "name", value);
      return this;
    }

    /**
     * Set the description.
     *
     * @param value a text description of the tileset. The description can contain any legal
     *        character. Implementations SHOULD NOT interpret the name as HTML!
     * @return this builder
     */
    public Builder setDescription(String value) {
      putIgnoreErrors(tileJson, "description", value);
      return this;
    }

    /**
     * Set the attribution.
     *
     * @param value the attribution to be displayed when the map is shown to a user. Implementations
     *        MAY decide to treat this as HTML or literal text. For security reasons, make
     *        absolutely sure that this field can't be abused as a vector for XSS or beacon
     *        tracking.
     * @return this builder
     */
    public Builder setAttribution(String value) {
      putIgnoreErrors(tileJson, "attribution", value);
      return this;
    }

    // #################################################################################
    // DATA DERIVED VALUES
    // #################################################################################
    public Builder setMinZoom(int value) {
      putIgnoreErrors(tileJson, "minzoom", value);
      return this;
    }

    public Builder setMaxZoom(int value) {
      putIgnoreErrors(tileJson, "maxzoom", value);
      return this;
    }

    public Builder setCenter(double lat, double lon, double zoom) {
      putIgnoreErrors(tileJson, "center", new double[] {lon, lat, zoom});
      return this;
    }

    /**
     * Set the bounds.
     *
     * @param west e.g. -180
     * @param north e.g. 90
     * @param east e.g. 180
     * @param south e.g. -90
     * @return this builder
     */
    public Builder setBounds(double west, double south, double east, double north) {
      try {
        final JSONArray bounds =
            new JSONArray(String.format(Locale.ENGLISH, "[%f,%f,%f,%f]", west, south, east, north));
        tileJson.put("bounds", bounds);
      } catch (final JSONException ex) {
        LOG.error("cannot add JSON attribute(s)", ex);
      }
      return this;
    }

    /**
     * Add a layer.
     *
     * @param layer the layer to add
     * @return this builder
     */
    public Builder addLayer(Layer layer) {
      try {
        if (!tileJson.has("vector_layers")) {
          tileJson.put("vector_layers", new JSONArray());
        }
        final JSONArray vectorLayers = tileJson.getJSONArray("vector_layers");
        vectorLayers.put(layer.getJson());
      } catch (final JSONException ex) {
        LOG.error("problem adding layer", ex);
      }
      return this;
    }

    /**
     * Set the type.
     *
     * @param value e.g. PBF
     * @return this builder
     */
    public Builder setType(String value) {
      putIgnoreErrors(tileJson, "type", value);
      return this;
    }

    private boolean isWithinZoomRange(int value) {
      return (0 <= value && value <= 22);
    }

    /**
     * Builder for a Metadata object.
     *
     * @return the metadata
     */
    public Metadata build() {

      final int minZoom = getIntegerIgnoreErrors("minzoom", tileJson, 0);
      final int maxZoom = getIntegerIgnoreErrors("maxzoom", tileJson, 22);

      final boolean isValidZoom =
          isWithinZoomRange(minZoom) && isWithinZoomRange(maxZoom) && minZoom <= maxZoom;
      if (!isValidZoom) {
        LOG.info("invalid zoom range: {} - {}. Ignoring parameters outside 0 - 22", minZoom,
            maxZoom);
        tileJson.remove("minzoom");
        tileJson.remove("maxzoom");
      }

      final double[] bounds =
          getDoubleArrayIgnoreErrors("bounds", tileJson, new double[] {-180, -90, 180, 90});
      final double[] center = getDoubleArrayIgnoreErrors("center", tileJson, null);

      final boolean isCenter = center != null;
      if (isCenter) {
        final boolean isWithinBounds =
            bounds[WEST] <= center[X] && center[X] <= bounds[EAST] && bounds[SOUTH] <= center[Y]
                && center[Y] <= bounds[NORTH] && minZoom <= center[Z] && center[Z] <= maxZoom;
        if (!isWithinBounds) {
          LOG.info("center is outside of bounds - ignoring!");
          tileJson.remove("center");
        }
      }

      final boolean isFormat = tileJson.has("format");
      if (!isFormat) {
        try {
          LOG.warn("format attribute was unspecified - using pbf");
          tileJson.put("format", "pbf");
        } catch (final JSONException ex) {
          LOG.info("cannot assign format type");
        }
      }

      return new Metadata(tileJson);
    }
  }

}
