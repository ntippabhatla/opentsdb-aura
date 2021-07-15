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

package net.opentsdb.aura.metrics.core.gorilla;

import net.opentsdb.aura.metrics.core.downsample.AggregationLengthIterator;
import net.opentsdb.aura.metrics.core.downsample.Aggregator;
import net.opentsdb.aura.metrics.core.downsample.AverageAggregator;
import net.opentsdb.aura.metrics.core.downsample.CountAggregator;
import net.opentsdb.aura.metrics.core.downsample.DownSampler;
import net.opentsdb.aura.metrics.core.downsample.Interval;
import net.opentsdb.aura.metrics.core.downsample.SegmentWidth;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GorillaDownSampledTimeSeriesEncoderTest {

  private static GorillaDownSampledTimeSeriesEncoder encoder;

  private static final int SEGMENT_TIMESTAMP = 1620237600;

  @Test
  void addAverage() {
    Interval interval = Interval._30_SEC;
    SegmentWidth segmentWidth = SegmentWidth._2_HR;

    int intervalWidth = interval.getWidth();
    short intervalCount = interval.getCount(segmentWidth);

    Aggregator aggregator = Aggregator.newBuilder(intervalCount).avg().build();
    DownSampler downSampler = new DownSampler(intervalWidth, intervalCount, aggregator);

    OffHeapGorillaDownSampledSegment segment = new OffHeapGorillaDownSampledSegment(256, null);
    encoder =
        new GorillaDownSampledTimeSeriesEncoder(
            false, interval, segmentWidth, downSampler, segment);

    double[] values =
        new double[] {
          0.8973950653568595,
          0.43654855440361706,
          0.827450779634358,
          0.3584920510780665,
          0.9295624657514724,
          0.9610921547553934,
          0.6329804921575314,
          0.34905996592724153,
          0.5379730703355181,
          0.8403559626106764,
          0.30075147324566376,
          0.15691026481149195,
          0.7525354276869367,
          0.942970394430076,
          0.2401190623680185,
          0.42611404794594654,
          0.7615746658524079,
          0.46418976228229414,
          0.6942765189361159,
          0.9690728790734268,
          0.32890435244089244,
          0.6098703276841767,
          0.22878432168195317,
          0.8844305249065624,
          0.7157591580282211
        };

    int segmentTime = 1611288000;
    double[] rawValues = new double[7200];
    Arrays.fill(rawValues, Double.NaN);
    for (int i = 0; i < rawValues.length; i++) {
      if (i % intervalWidth == 0) {
        rawValues[i] = values[i % values.length];
      }
    }

    downSampler.apply(rawValues);

    Iterator<double[]> iterator = downSampler.iterator();
    double[] expectedAvg = iterator.next();

    encoder.createSegment(segmentTime);

    encoder.addDataPoints(rawValues);

    AggregationLengthIterator aggLengthIterator = encoder.aggIterator();
    aggLengthIterator.next();
    assertEquals(16218, aggLengthIterator.aggLength());
    assertEquals(AverageAggregator.ID, aggLengthIterator.aggID());
    assertEquals(AverageAggregator.NAME, aggLengthIterator.aggName());
    assertFalse(aggLengthIterator.hasNext());

    double[] aggValues = new double[expectedAvg.length];

    encoder.readAggValues(aggValues, aggregator.getId());

    assertArrayEquals(expectedAvg, aggValues);
  }

  @Test
  void addAverageAndCount() {
    Interval interval = Interval._30_SEC;
    SegmentWidth segmentWidth = SegmentWidth._2_HR;

    int intervalWidth = interval.getWidth();
    short intervalCount = interval.getCount(segmentWidth);

    Aggregator aggregator = Aggregator.newBuilder(intervalCount).avg().count().build();
    DownSampler downSampler = new DownSampler(intervalWidth, intervalCount, aggregator);

    OffHeapGorillaDownSampledSegment segment = new OffHeapGorillaDownSampledSegment(256, null);
    encoder =
        new GorillaDownSampledTimeSeriesEncoder(
            false, interval, segmentWidth, downSampler, segment);

    double[] values =
        new double[] {
          0.8973950653568595,
          0.43654855440361706,
          0.827450779634358,
          0.3584920510780665,
          0.9295624657514724,
          0.9610921547553934,
          0.6329804921575314,
          0.34905996592724153,
          0.5379730703355181,
          0.8403559626106764,
          0.30075147324566376,
          0.15691026481149195,
          0.7525354276869367,
          0.942970394430076,
          0.2401190623680185,
          0.42611404794594654,
          0.7615746658524079,
          0.46418976228229414,
          0.6942765189361159,
          0.9690728790734268,
          0.32890435244089244,
          0.6098703276841767,
          0.22878432168195317,
          0.8844305249065624,
          0.7157591580282211
        };

    int segmentTime = 1611288000;
    double[] rawValues = new double[7200];
    Arrays.fill(rawValues, Double.NaN);
    for (int i = 0; i < rawValues.length; i++) {
      if (i % intervalWidth == 0) {
        rawValues[i] = values[i % values.length];
      }
    }

    downSampler.apply(rawValues);

    Iterator<double[]> iterator = downSampler.iterator();
    double[] expectedAvgs = iterator.next();
    double[] expectedCounts = iterator.next();

    encoder.createSegment(segmentTime);

    encoder.addDataPoints(rawValues);

    AggregationLengthIterator aggLengthIterator = encoder.aggIterator();
    aggLengthIterator.next();
    assertEquals(16218, aggLengthIterator.aggLength());
    assertEquals(AverageAggregator.ID, aggLengthIterator.aggID());
    assertEquals(AverageAggregator.NAME, aggLengthIterator.aggName());

    double[] aggValues = new double[expectedAvgs.length];
    encoder.readAggValues(aggValues, aggLengthIterator.aggID());
    assertArrayEquals(expectedAvgs, aggValues);

    aggLengthIterator.next();
    assertEquals(303, aggLengthIterator.aggLength());
    assertEquals(CountAggregator.ID, aggLengthIterator.aggID());
    assertEquals(CountAggregator.NAME, aggLengthIterator.aggName());

    //    double[] countValues = new double[expectedCounts.length];
    //    encoder.readAggValues(countValues, aggLengthIterator.aggID());
    //    assertArrayEquals(expectedCounts, countValues);

    assertFalse(aggLengthIterator.hasNext());
  }
}