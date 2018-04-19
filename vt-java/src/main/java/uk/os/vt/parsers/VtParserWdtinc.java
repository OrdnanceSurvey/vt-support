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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import uk.os.vt.Entry;
import uk.os.vt.mvt.adapt.jts.MvtReader;
import uk.os.vt.mvt.adapt.jts.TagKeyValueMapConverter;
import uk.os.vt.mvt.adapt.jts.model.JtsLayer;
import uk.os.vt.mvt.adapt.jts.model.JtsMvt;

public class VtParserWdtinc implements VtParser {

  private static final GeometryFactory GEOMETRY_FACORY = new GeometryFactory();

  @Override
  public List<Geometry> parse(Entry entry) throws IOException {
    // TODO REMOVE THIS OBSOLETE CLASS - it flattens layers
    final byte[] bytes = entry.getVector();
    final InputStream is = new ByteArrayInputStream(bytes);

    List<Geometry> allgeoms = new ArrayList<>();
    JtsMvt result = MvtReader.loadMvt(is, GEOMETRY_FACORY, new TagKeyValueMapConverter());
    for (JtsLayer l : result.getLayers()) {
      allgeoms.addAll(l.getGeometries());
    }

    return allgeoms;
  }
}
