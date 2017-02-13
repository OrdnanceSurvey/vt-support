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
import java.io.IOException;

import uk.os.vt.Storage;
import uk.os.vt.mbtiles.StorageImpl;

public final class StorageUtil {

  private StorageUtil() {}

  /**
   * Get a storage reference.
   *
   * @param file a directory or mbtiles reference
   * @return an appropriate storage adapter
   * @throws IOException if no storage adapter for the location or the naming convention is not
   *         adhered to
   */
  public static Storage getStorage(File file) throws IOException {
    final boolean isMbtilesTile = file.getName().toLowerCase().contains(".mbtiles");
    if (isMbtilesTile) {
      return new StorageImpl.Builder(file).build();
    }

    final boolean isFileSystem = file.isDirectory();
    if (isFileSystem) {
      return new uk.os.vt.filesystem.StorageImpl.Builder(file).build();
    }

    throw new IOException("unknown storage provider for file reference " + file.getAbsolutePath());
  }
}
