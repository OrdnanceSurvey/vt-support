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

public final class BooleanSummary {

  private final long trues;
  private final long falses;
  private final long samples;

  /**
   * Enables {@link Boolean} values to be summarised.
   */
  public BooleanSummary(boolean... vars) {
    throwIfEmpty(vars);
    long trues = 0;
    long falses = 0;
    this.samples = vars.length;

    for (int i = 0; i < vars.length; i++) {
      if (vars[i]) {
        trues++;
      } else {
        falses++;
      }
    }
    this.trues = trues;
    this.falses = falses;
  }

  /**
   * Summary (aggregate) of {@link Boolean values}.
   *
   * @param vars list of existing summaries to be combined
   */
  public BooleanSummary(BooleanSummary... vars) {
    throwIfEmpty(vars);
    long trues = vars[0].trues;
    long falses = vars[0].falses;
    long samples = vars[0].samples;

    for (int i = 1; i < vars.length; i++) {
      trues += vars[i].trues;
      falses += vars[i].falses;
      samples += vars[i].samples;
    }

    this.trues = trues;
    this.falses = falses;
    this.samples = samples;
  }

  /**
   * Summary (aggregate) of {@link Boolean values}.
   *
   * @param summary existing summary
   * @param value boolean attribute to combine
   */
  public BooleanSummary(BooleanSummary summary, boolean value) {
    Objects.requireNonNull(summary);
    long trues = summary.trues;
    long falses = summary.falses;
    if (value) {
      trues++;
    } else {
      falses++;
    }
    long samples = summary.samples + 1;

    this.trues = trues;
    this.falses = falses;
    this.samples = samples;
  }

  /**
   * Summary (aggregate) of {@link Boolean values}.
   *
   * @param trues total number of true values
   * @param falses total number of false values
   * @param samples total number of samples
   */
  public BooleanSummary(long trues, long falses, long samples) {
    this.trues = trues;
    this.falses = falses;
    this.samples = samples;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    BooleanSummary that = (BooleanSummary) obj;
    return trues == that.trues
        && falses == that.falses
        && samples == that.samples;
  }

  public long getTrues() {
    return trues;
  }

  public long getFalses() {
    return falses;
  }

  public long getSamples() {
    return samples;
  }

  @Override
  public int hashCode() {
    return Objects.hash(trues, falses, samples);
  }

  @Override
  public String toString() {
    return "BooleanSummary{"
        + "trues=" + trues
        + ", falses=" + falses
        + ", samples=" + samples
        + '}';
  }

  private static void throwIfEmpty(boolean... vars) {
    Objects.requireNonNull(vars);
    if (vars.length == 0) {
      throw new IllegalArgumentException("nothing to merge!");
    }
  }

  private static void throwIfEmpty(BooleanSummary... vars) {
    Objects.requireNonNull(vars);
    if (vars.length == 0) {
      throw new IllegalArgumentException("nothing to merge!");
    }
  }
}
