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

/**
 * See: <a href="https://developers.google.com/protocol-buffers/docs/encoding#types">Google Protocol
 * Buffers Docs</a>
 */
public final class ZigZag {

  /**
   * See: <a href="https://developers.google.com/protocol-buffers/docs/encoding#types">Google
   * Protocol Buffers Docs</a>
   *
   * @param number integer to encode
   * @return zig-zag encoded integer
   */
  public static int encode(int number) {
    return (number << 1) ^ (number >> 31);
  }

  /**
   * See: <a href="https://developers.google.com/protocol-buffers/docs/encoding#types">Google
   * Protocol Buffers Docs</a>
   *
   * @param number zig-zag encoded integer to decode
   * @return decoded integer
   */
  public static int decode(int number) {
    return (number >> 1) ^ (-(number & 1));
  }
}
