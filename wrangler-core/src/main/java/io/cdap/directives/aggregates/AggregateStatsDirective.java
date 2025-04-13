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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * Directive for aggregating byte size and time duration statistics.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates byte size and time duration statistics from specified columns.")
public class AggregateStatsDirective implements Directive {
  public static final String NAME = "aggregate-stats";
  private String sizeColumn;
  private String timeColumn;
  private String totalSizeColumn;
  private String totalTimeColumn;
  private String sizeUnit;
  private String timeUnit;
  private double totalBytes;
  private double totalMilliseconds;
  private int rowCount;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("size_column", TokenType.COLUMN_NAME);
    builder.define("time_column", TokenType.COLUMN_NAME);
    builder.define("total_size_column", TokenType.COLUMN_NAME);
    builder.define("total_time_column", TokenType.COLUMN_NAME);
    builder.define("size_unit", TokenType.TEXT, "Output size unit (B, KB, MB, GB, TB)");
    builder.define("time_unit", TokenType.TEXT, "Output time unit (ms, s, m, h, d)");
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumn = args.value("size_column");
    this.timeColumn = args.value("time_column");
    this.totalSizeColumn = args.value("total_size_column");
    this.totalTimeColumn = args.value("total_time_column");
    this.sizeUnit = args.value("size_unit", "MB");
    this.timeUnit = args.value("time_unit", "s");
    this.totalBytes = 0.0;
    this.totalMilliseconds = 0.0;
    this.rowCount = 0;
  }

  @Override
  public void destroy() {
    // No resources to clean up
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      Object sizeObj = row.getValue(sizeColumn);
      Object timeObj = row.getValue(timeColumn);

      if (sizeObj != null) {
        ByteSize byteSize = new ByteSize(sizeObj.toString());
        totalBytes += byteSize.getBytes();
      }

      if (timeObj != null) {
        TimeDuration timeDuration = new TimeDuration(timeObj.toString());
        totalMilliseconds += timeDuration.getMilliseconds();
      }

      rowCount++;
    }

    // Create a new row with the aggregated statistics
    Row resultRow = new Row();
    resultRow.add(totalSizeColumn, convertBytes(totalBytes, sizeUnit));
    resultRow.add(totalTimeColumn, convertTime(totalMilliseconds, timeUnit));

    return List.of(resultRow);
  }

  private double convertBytes(double bytes, String unit) {
    switch (unit.toUpperCase()) {
      case "B":
        return bytes;
      case "KB":
        return bytes / 1024.0;
      case "MB":
        return bytes / (1024.0 * 1024.0);
      case "GB":
        return bytes / (1024.0 * 1024.0 * 1024.0);
      case "TB":
        return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
      default:
        throw new IllegalArgumentException("Unsupported byte unit: " + unit);
    }
  }

  private double convertTime(double milliseconds, String unit) {
    switch (unit.toLowerCase()) {
      case "ms":
        return milliseconds;
      case "s":
        return milliseconds / 1000.0;
      case "m":
        return milliseconds / (60.0 * 1000.0);
      case "h":
        return milliseconds / (60.0 * 60.0 * 1000.0);
      case "d":
        return milliseconds / (24.0 * 60.0 * 60.0 * 1000.0);
      default:
        throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }
  }
} 