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

package uk.os.vt.mvt.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import uk.os.vt.mvt.encoding.ZigZag;

/**
 * Test zig zag encoding function.
 */
public final class ZigZagTest {

  @Test
  public void encodeAndDecode() {
    assertEquals(ZigZag.decode(ZigZag.encode(0)), 0);
    assertEquals(ZigZag.decode(ZigZag.encode(10018754)), 10018754);
  }
}
