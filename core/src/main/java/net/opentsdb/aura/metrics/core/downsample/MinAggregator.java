/*
 * This file is part of OpenTSDB.
 * Copyright (C) 2021  Yahoo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.opentsdb.aura.metrics.core.downsample;

public class MinAggregator extends Aggregator {

  public static final byte ID = (byte) 0b1000;
  public static final String NAME = "min";

  public MinAggregator(final int numPoints) {
    this(numPoints, null);
  }

  public MinAggregator(final int numPoints, final Aggregator aggregator) {
    super(Double.MAX_VALUE, ID, NAME, numPoints, aggregator);
  }

  @Override
  public void doApply(double value) {
    if (value < this.value) {
      this.value = value;
    }
  }
}
