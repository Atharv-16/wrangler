/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link AggregateStatsDirective}
 */
public class AggregateStatsDirectiveTest {

  @Test
  public void testBasicAggregation() throws DirectiveParseException, DirectiveExecutionException {
    String[] directives = new String[] {
      "aggregate-stats :size :time total_size total_time 'MB' 's'"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", "100MB", "time", "5s"),
      createRow("size", "200MB", "time", "10s"),
      createRow("size", "300MB", "time", "15s")
    );

    List<Row> results = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    Assert.assertEquals(600.0, result.getValue("total_size"), 0.001);
    Assert.assertEquals(30.0, result.getValue("total_time"), 0.001);
  }

  @Test
  public void testMixedUnits() throws DirectiveParseException, DirectiveExecutionException {
    String[] directives = new String[] {
      "aggregate-stats :size :time total_size total_time 'GB' 'm'"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", "1024MB", "time", "120s"),
      createRow("size", "2GB", "time", "5m"),
      createRow("size", "512MB", "time", "180s")
    );

    List<Row> results = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    // Expected: (1024MB + 2048MB + 512MB) = 3584MB = 3.5GB
    Assert.assertEquals(3.5, result.getValue("total_size"), 0.001);
    // Expected: (120s + 300s + 180s) = 600s = 10m
    Assert.assertEquals(10.0, result.getValue("total_time"), 0.001);
  }

  @Test
  public void testNullValues() throws DirectiveParseException, DirectiveExecutionException {
    String[] directives = new String[] {
      "aggregate-stats :size :time total_size total_time 'KB' 'ms'"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", "1024B", "time", null),
      createRow("size", null, "time", "1s"),
      createRow("size", "2KB", "time", "500ms")
    );

    List<Row> results = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    // Expected: (1KB + 2KB) = 3KB
    Assert.assertEquals(3.0, result.getValue("total_size"), 0.001);
    // Expected: (1000ms + 500ms) = 1500ms
    Assert.assertEquals(1500.0, result.getValue("total_time"), 0.001);
  }

  @Test(expected = DirectiveExecutionException.class)
  public void testInvalidByteUnit() throws DirectiveParseException, DirectiveExecutionException {
    String[] directives = new String[] {
      "aggregate-stats :size :time total_size total_time 'XB' 's'"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", "100MB", "time", "5s")
    );

    TestingRig.execute(directives, rows);
  }

  @Test(expected = DirectiveExecutionException.class)
  public void testInvalidTimeUnit() throws DirectiveParseException, DirectiveExecutionException {
    String[] directives = new String[] {
      "aggregate-stats :size :time total_size total_time 'MB' 'x'"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", "100MB", "time", "5s")
    );

    TestingRig.execute(directives, rows);
  }

  private Row createRow(String... keyValues) {
    Row row = new Row();
    for (int i = 0; i < keyValues.length; i += 2) {
      row.add(keyValues[i], keyValues[i + 1]);
    }
    return row;
  }
} 