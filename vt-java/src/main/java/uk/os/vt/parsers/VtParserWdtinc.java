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

package uk.os.vt.parsers;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TagKeyValueMapConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import uk.os.vt.Entry;

public class VtParserWdtinc implements VtParser {

  private static final GeometryFactory GEOMETRY_FACORY = new GeometryFactory();

  @Override
  public List<Geometry> parse(Entry entry) throws IOException {
    final byte[] bytes = entry.getVector();
    final InputStream is = new ByteArrayInputStream(bytes);
    return MvtReader.loadMvt(is, GEOMETRY_FACORY, new TagKeyValueMapConverter());
  }
}
