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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class EntryTest {

  private static final int MAX_ZOOM = 22;
  private static final int MIN_ZOOM = 0;

  // Min is 2^0
  private static final int MIN_AXIS_VALUE = 1;
  // Max is 2^22
  private static final int MAX_AXIS_VALUE = 4194304;

  @Test
  public void testConstruction() {
    final int zoom = 10;
    final int column = 45;
    final int row = 90;
    final byte[] bytes = getGarbageBytes();
    final Entry entry = new Entry(zoom, column, row, bytes);

    final int expectedZoom = zoom;
    final int actualZoom = entry.getZoomLevel();
    assertEquals(expectedZoom, actualZoom);

    final int expectedColumn = column;
    final int actualColumn = entry.getColumn();
    assertEquals(expectedColumn, actualColumn);

    final int expectedRow = row;
    final int actualRow = entry.getRow();
    assertEquals(expectedRow, actualRow);

    final byte[] expectedBytes = bytes;
    final byte[] actualBytes = entry.getVector();
    assertArrayEquals(expectedBytes, actualBytes);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidZoomLevelMax() {
    new Entry(MAX_ZOOM + 1, 1, 1, getGarbageBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidZoomLevelMin() {
    new Entry(MIN_ZOOM - 1, 1, 1,
        getGarbageBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidZoomLevelMaxAcceptable() {
    new Entry(MAX_ZOOM, MAX_AXIS_VALUE, MAX_AXIS_VALUE, getGarbageBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidZoomLevelMinAcceptable() {
    new Entry(MIN_ZOOM, MIN_AXIS_VALUE, MIN_AXIS_VALUE, getGarbageBytes());
  }

  private byte[] getGarbageBytes() {
    try {
      return "test".getBytes("UTF-8");
    } catch (UnsupportedEncodingException exception) {
      return new byte[]{};
    }
  }
}
