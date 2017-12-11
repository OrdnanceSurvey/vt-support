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

import java.util.List;
import uk.os.vt.mvt.VectorTile;

/**
 * Process MVT tags and feature id, convert to user data object. The returned user data
 * object may be null.
 */
public interface ITagConverter {

  /**
   * Convert MVT user data to JTS user data object or null.
   *
   * @param id         feature id, may be {@code null}
   * @param tags       MVT feature tags, may be invalid
   * @param keysList   layer key list
   * @param valuesList layer value list
   * @return user data object or null
   */
  Object toUserData(Long id,
                    List<Integer> tags,
                    List<String> keysList,
                    List<VectorTile.Tile.Value> valuesList);
}
