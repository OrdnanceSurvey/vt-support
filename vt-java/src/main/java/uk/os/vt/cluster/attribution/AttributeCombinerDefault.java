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

import uk.os.vt.cluster.attribution.types.BooleanSummary;
import uk.os.vt.cluster.attribution.types.DoubleSummary;
import uk.os.vt.cluster.attribution.types.StringSummary;

/**
 * Default combiner used when no other combiner is available.
 */
class AttributeCombinerDefault implements AttributeCombiner<Object> {
  @Override
  public Object combine(Object existingInput, Object item) {
    if (existingInput instanceof DoubleSummary) {
      if (item instanceof Number) {
        return new DoubleSummary(((DoubleSummary) existingInput),
            (((Number) item).doubleValue()));
      } else if (item instanceof DoubleSummary) {
        return new DoubleSummary(((DoubleSummary) existingInput), ((DoubleSummary) item));
      }
    }

    if (existingInput instanceof Number) {
      if (item instanceof Number) {
        return new DoubleSummary(((Number) existingInput).doubleValue(),
            ((Number) item).doubleValue());
      } else if (item instanceof DoubleSummary) {
        return new DoubleSummary(((DoubleSummary) item),
            ((Number) existingInput).doubleValue());
      }
    }

    if (existingInput instanceof StringSummary) {
      if (item instanceof String) {
        return new StringSummary((StringSummary) existingInput, (String) item);
      } else if (item instanceof StringSummary) {
        return new StringSummary((StringSummary) existingInput, (StringSummary) item);
      }
    }

    if (existingInput instanceof String) {
      if (item instanceof String) {
        return new StringSummary((String)existingInput, (String) item);
      } else if (item instanceof StringSummary) {
        return new StringSummary((StringSummary) item, (String) existingInput);
      }
    }

    if (existingInput instanceof BooleanSummary) {
      if (item instanceof Boolean) {
        return new BooleanSummary((BooleanSummary) existingInput, (boolean) item);
      } else if (item instanceof BooleanSummary) {
        return new BooleanSummary((BooleanSummary) existingInput, (BooleanSummary) item);
      }
    }

    if (existingInput instanceof Boolean) {
      if (item instanceof Boolean) {
        return new BooleanSummary((boolean)existingInput, (boolean) item);
      } else if (item instanceof BooleanSummary) {
        return new BooleanSummary((BooleanSummary) item, (boolean) existingInput);
      }
    }
    throw new IllegalArgumentException("Cannot combine: \"" + existingInput + "\" with \""
        + item + "\"");
  }
}
