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

package uk.os.vt.cluster.attribution;

import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.csv.CSVFormat;

public final class StringSummary {

  /**
   * The max total length of all sampled strings within a set.
   * Note: this is not the serialized size as that is undefined.
   */
  private static final int DEFAULT_MAX_SAMPLE_LENGTH = 100;

  private final long samples;
  private final SortedSet<String> sample;

  /**
   * Aggregate multiple {@link StringSummary} items.
   *
   * @param vars at least one {@link StringSummary}
   */
  public StringSummary(StringSummary... vars) {
    Internal.throwIfEmpty(vars);

    SortedSet<String> newSample = new TreeSet<>(vars[0].getSample());
    long samples = vars[0].samples;

    for (int i = 1; i < vars.length; i++) {
      newSample.addAll(vars[i].getSample());
      samples += vars[i].samples;
    }
    this.sample = newSample;
    this.samples = samples;
  }

  /**
   * Aggregate a {@link StringSummary} with a {@link String}.
   * @param summary an existing {@link StringSummary}
   * @param value a String to merge with the {@link StringSummary}
   */
  public StringSummary(StringSummary summary, String value) {
    Objects.requireNonNull(summary);
    SortedSet<String> newSample = new TreeSet<>(summary.getSample());
    newSample.add(value);
    long samples = summary.samples + 1;
    this.sample = newSample;
    this.samples = samples;
  }

  public StringSummary(String... values) {
    this(Internal.throwIfEmpty(values), values.length);
  }

  public StringSummary(String[] vars, long samples) {
    this(smallSample(vars), samples);
  }

  /**
   * A summary aggregate of String values
   *
   * @param sample a subset of the aggregated values
   * @param samples the count of all aggregated {@link String} values.
   */
  public StringSummary(SortedSet<String> sample, long samples) {
    this.sample = sample;
    this.samples = samples;
  }

  public String getValue() {
    return Internal.setToValue(sample);
  }

  public SortedSet<String> getSample() {
    return sample;
  }

  public long getTotalSamples() {
    return samples;
  }

  @Override
  public String toString() {
    return "StringSummary{"
        + "samples=" + samples
        + ", sample=" + sample
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    StringSummary that = (StringSummary) obj;
    return samples == that.samples
        && Objects.equals(sample, that.sample);
  }

  @Override
  public int hashCode() {
    return Objects.hash(samples, sample);
  }

  private static SortedSet<String> smallSample(String... vars) {
    // only consider the first 100 characters as the "sample"
    SortedSet<String> sample = new TreeSet<>();
    int size = 0;
    for (int i = 0; i < vars.length; i++) {
      String word = vars[i];
      // the string is already in the set, so we ignore
      boolean isAlreadyValue = sample.contains(word);
      if (!isAlreadyValue && !word.isEmpty()) {
        // we use 100 characters as the arbitrary sample size (not serialized size).
        size += word.length();
        if (size > DEFAULT_MAX_SAMPLE_LENGTH) {
          break;
        }
        sample.add(word);
      }
    }
    return sample;
  }

  private static class Internal {

    private static String setToValue(SortedSet<String> sample) {
      StringBuilder sb = new StringBuilder(sample.size());
      Iterator<String> iterator = sample.iterator();
      if (iterator.hasNext()) {
        String first = iterator.next();
        sb.append(CSVFormat.RFC4180.format(first));
      }
      while (iterator.hasNext()) {
        String item = iterator.next();
        sb.append(", ").append(CSVFormat.RFC4180.format(item));
      }
      return sb.toString();
    }

    private static String[] throwIfEmpty(String... vars) {
      if (vars.length == 0) {
        throw new IllegalArgumentException("nothing to merge!");
      }
      return vars;
    }

    private static void throwIfEmpty(StringSummary... vars) {
      if (vars.length == 0) {
        throw new IllegalArgumentException("nothing to merge!");
      }
    }
  }
}
