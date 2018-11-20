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

import java.util.Map;
import uk.os.vt.cluster.attribution.types.DoubleSummary;

public interface AttributionSerializer {

  /**
   * Returns a Map without complex attribute objects such as {@link DoubleSummary}.
   *
   * <p>Upon serialisation, {@link DoubleSummary} attribute values are expanded into attribute
   * max, min, sum, total values.</p>
   *
   * @param values the geometry attributes
   * @return the attributes including any {@link DoubleSummary} values
   */
  default Map<String, Object> serialize(Map<String, Object> values) {
    return AttributionSerializerImpl.serialize(values);
  }
}
