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

package uk.os.vt.cluster.attribution.types;

import java.util.Objects;

public final class DoubleSummary {

  private final double max;
  private final double min;
  private final long samples;
  private final double sum;

  /**
   * Merge another entry with this {@link DoubleSummary}.
   *
   * @param summary {@link DoubleSummary} to combine
   * @param value the extra value aggregate
   */
  public DoubleSummary(DoubleSummary summary, double value) {
    Objects.requireNonNull(summary);
    this.max = Math.max(summary.max, value);
    this.min = Math.min(summary.min, value);
    this.samples = summary.samples + 1;
    this.sum = summary.sum + value;
  }

  /**
   * Merge multiple {@link DoubleSummary} items.
   *
   * @param vars at least one {@link DoubleSummary}
   */
  public DoubleSummary(DoubleSummary... vars) {
    throwIfEmpty(vars);
    double max = vars[0].max;
    double min = vars[0].min;
    long samples = vars[0].samples;
    double sum = vars[0].sum;

    for (int i = 1; i < vars.length; i++) {
      max = Math.max(max, vars[i].max);
      min = Math.min(min, vars[i].min);
      samples += vars[i].samples;
      sum += vars[i].sum;
    }
    this.max = max;
    this.min = min;
    this.samples = samples;
    this.sum = sum;
  }

  /**
   * Merge multiple doubles.
   *
   * @param vars at least one double to aggregate
   */
  public DoubleSummary(double... vars) {
    throwIfEmpty(vars);
    double max = vars[0];
    double min = vars[0];
    long samples = 1;
    double sum = vars[0];

    for (int i = 1; i < vars.length; i++) {
      max = Math.max(max, vars[i]);
      min = Math.min(min, vars[i]);
      samples++;
      sum += vars[i];
    }
    this.max = max;
    this.min = min;
    this.samples = samples;
    this.sum = sum;
  }

  /**
   * Construct a new summary.
   *
   * @param max maximum value encountered in the cluster
   * @param min minimum value encountered in the cluster
   * @param samples the total number of sample combined within the cluster
   * @param sum the addition of all values within the cluster
   */
  public DoubleSummary(double max, double min, long samples, double sum) {
    this.max = max;
    this.min = min;
    this.samples = samples;
    this.sum = sum;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    DoubleSummary that = (DoubleSummary) obj;
    return Double.compare(that.max, max) == 0
        && Double.compare(that.min, min) == 0
        && samples == that.samples
        && Double.compare(that.sum, sum) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(max, min, samples, sum);
  }

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  public double getSum() {
    return sum;
  }

  public long getTotalSamples() {
    return samples;
  }

  public double getMean() {
    return sum / samples;
  }

  @Override
  public String toString() {
    return String.format("[max: %f, min: %f, samples: %d, sum: %f]", max, min, samples, sum);
  }

  private static void throwIfEmpty(double... vars) {
    if (vars.length == 0) {
      throw new IllegalArgumentException("nothing to merge!");
    }
  }

  private static void throwIfEmpty(DoubleSummary... vars) {
    if (vars.length == 0) {
      throw new IllegalArgumentException("nothing to merge!");
    }
  }
}
