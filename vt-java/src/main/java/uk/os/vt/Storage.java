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

import io.reactivex.Observable;

public interface Storage extends AutoCloseable, MetadataProvider {

  Observable<Entry> getEntries();

  Observable<Entry> getEntries(int zoom);

  Observable<Entry> getEntry(int zoom, int col, int row);

  Observable<Integer> getMaxZoomLevel();

  Observable<Integer> getMinZoomLevel();

  void putEntries(Observable<Entry> entries);

  Observable<StorageResult> put(Observable<Entry> entries);

  Observable<StorageResult> delete(Observable<Entry> entries);
}
