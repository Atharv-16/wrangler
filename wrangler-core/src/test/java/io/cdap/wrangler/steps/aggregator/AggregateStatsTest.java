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

package io.cdap.wrangler.steps.aggregator;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveLoadException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.RecipeException;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link AggregateStats} directive.
 */
public class AggregateStatsTest {

  @Test
  public void testBasicAggregation() throws DirectiveParseException, DirectiveExecutionException, 
      RecipeException, DirectiveLoadException {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time total_size total_time"
    };

    List<Row> rows = Arrays.asList(
      createRow("data_size", "100KB", "response_time", "500ms"),
      createRow("data_size", "200KB", "response_time", "750ms"),
      createRow("data_size", "300KB", "response_time", "1s")
    );

    List<Row> results = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    
    // 600KB = 0.5859375MB
    Assert.assertEquals(0.5859375, ((Number) result.getValue("total_size")).doubleValue(), 0.0001);
    // 2250ms = 2.25s
    Assert.assertEquals(2.25, ((Number) result.getValue("total_time")).doubleValue(), 0.0001);
  }

  @Test
  public void testAggregationWithCustomUnits() throws DirectiveParseException, DirectiveExecutionException,
      RecipeException, DirectiveLoadException {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time total_size_gb total_time_min 'GB' 'm'"
    };

    List<Row> rows = Arrays.asList(
      createRow("data_size", "1GB", "response_time", "30s"),
      createRow("data_size", "2GB", "response_time", "90s"),
      createRow("data_size", "1.5GB", "response_time", "120s")
    );

    List<Row> results = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    
    // 4.5GB
    Assert.assertEquals(4.5, ((Number) result.getValue("total_size_gb")).doubleValue(), 0.0001);
    // 240s = 4 minutes
    Assert.assertEquals(4.0, ((Number) result.getValue("total_time_min")).doubleValue(), 0.0001);
  }

  @Test
  public void testMixedUnits() throws DirectiveParseException, DirectiveExecutionException,
      RecipeException, DirectiveLoadException {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time total_size_mb total_time_s"
    };

    List<Row> rows = Arrays.asList(
      createRow("data_size", "1024KB", "response_time", "1.5s"),
      createRow("data_size", "1MB", "response_time", "500ms"),
      createRow("data_size", "0.5MB", "response_time", "750ms")
    );

    List<Row> results = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    
    // 1024KB + 1MB + 0.5MB = 2.5MB
    Assert.assertEquals(2.5, ((Number) result.getValue("total_size_mb")).doubleValue(), 0.0001);
    // 1.5s + 500ms + 750ms = 2.75s
    Assert.assertEquals(2.75, ((Number) result.getValue("total_time_s")).doubleValue(), 0.0001);
  }

  @Test(expected = DirectiveExecutionException.class)
  public void testInvalidByteSize() throws DirectiveParseException, DirectiveExecutionException,
      RecipeException, DirectiveLoadException {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time total_size total_time"
    };

    List<Row> rows = Arrays.asList(
      createRow("data_size", "invalid", "response_time", "500ms")
    );

    TestingRig.execute(directives, rows);
  }

  @Test(expected = DirectiveExecutionException.class)
  public void testInvalidTimeDuration() throws DirectiveParseException, DirectiveExecutionException,
      RecipeException, DirectiveLoadException {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time total_size total_time"
    };

    List<Row> rows = Arrays.asList(
      createRow("data_size", "100KB", "response_time", "invalid")
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
