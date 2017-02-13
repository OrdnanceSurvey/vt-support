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

package uk.os.vt.demo.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceUtil {

  private ResourceUtil() {}

  private static final Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);

  /**
   * Get a file.
   *
   * @param file the file
   * @return a file reference
   */
  public static File getFile(String file) {
    final URL result = ResourceUtil.class.getClassLoader().getResource(file);
    try {
      Path path = Paths.get(result.toURI());
      return path.toFile();
    } catch (final URISyntaxException ex) {
      LOG.error("Cannot find resource: " + file);
      return null;
    }
  }
}
