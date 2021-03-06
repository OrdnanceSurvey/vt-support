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

public class StorageResult {

  private final Entry entry;
  private final boolean completed;
  private final Exception exception;

  /**
   * A result class to be returned after successful submission to storage.
   *
   * @param entry the tile that was successfully passed to storage
   */
  public StorageResult(Entry entry) {
    this.entry = entry;
    this.completed = true;
    this.exception = null;
  }

  /**
   * A result class to be returned after unsuccessful submission to storage.
   *
   * @param entry the tile that was unsuccessfully passed to storage
   * @param exception the exception that was thrown attempting to update the storage
   */
  public StorageResult(Entry entry, Exception exception) {
    this.entry = entry;
    this.completed = false;
    this.exception = exception;
  }

  public Entry getEntry() {
    return entry;
  }

  public boolean isCompleted() {
    return completed;
  }

  public Exception getException() {
    return exception;
  }
}
