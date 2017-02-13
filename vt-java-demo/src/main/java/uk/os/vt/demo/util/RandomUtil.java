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

import java.util.Random;

public final class RandomUtil {

  private RandomUtil() {}

  private static final Random RAND = new Random();

  @SafeVarargs
  public static <T> T getRandom(T... ts) {
    final int value = randInt(0, ts.length - 1);
    return ts[value];
  }

  /**
   * Source:
   * http://stackoverflow.com/questions/363681/generating-random-integers-in-a-specific-range
   * Returns a pseudo-random number between min and max, inclusive. The difference between min and
   * max can be at most <code>Integer.MAX_VALUE - 1</code>.
   *
   * @param min Minimum value
   * @param max Maximum value. Must be greater than min.
   * @return Integer between min and max, inclusive.
   * @see java.util.Random#nextInt(int)
   */
  public static int randInt(int min, int max) {
    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    final int randomNum = RAND.nextInt((max - min) + 1) + min;

    return randomNum;
  }
}
