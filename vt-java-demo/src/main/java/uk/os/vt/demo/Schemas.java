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

package uk.os.vt.demo;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.os.vt.Metadata;

public class Schemas {

  private static final Logger LOG = LoggerFactory.getLogger(Schemas.class.getSimpleName());

  public static class OutbreakSchemaV1 {

    private static final Metadata metadata;

    static {
      final URL url = Resources.getResource("outbreak_schema_v1.json");
      Metadata inflated;
      try {
        String schemaString = Resources.toString(url, Charsets.UTF_8);
        inflated = new Metadata.Builder().setTileJson(new JSONObject(schemaString)).build();
      } catch (final IOException ex) {
        LOG.error("problem inflating local schema", ex);
        inflated = new Metadata.Builder().build();
      } catch (JSONException je) {
        LOG.error("problem inflating JSON", je);
        inflated = new Metadata.Builder().build();
      }
      metadata = inflated;
    }

    public static Metadata get() {
      return metadata;
    }
  }
}
