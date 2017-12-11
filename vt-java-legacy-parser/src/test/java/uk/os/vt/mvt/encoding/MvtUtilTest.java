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

package uk.os.vt.mvt.encoding;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

/**
 * Test MVT utility functions.
 */
public final class MvtUtilTest {

  @Test
  public void testHeaders() {
    assertEquals(GeomCmdHdr.cmdHdr(GeomCmd.MoveTo, 1), 9);
    assertEquals(GeomCmdHdr.cmdHdr(GeomCmd.MoveTo, 1) >> 3, 1);

    assertEquals(GeomCmdHdr.getCmdId(GeomCmdHdr.cmdHdr(GeomCmd.MoveTo, 1)),
        GeomCmd.MoveTo.getCmdId());
    assertEquals(GeomCmdHdr.getCmdLength(GeomCmdHdr.cmdHdr(GeomCmd.MoveTo, 1)), 1);

    Arrays.stream(GeomCmd.values()).forEach(c -> assertEquals(GeomCmdHdr.cmdHdr(c, 1) & 0x7,
        c.getCmdId()));
  }
}
