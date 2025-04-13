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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive for aggregating byte sizes and time durations.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates byte sizes and time durations from specified columns.")
public class AggregateStats implements Directive {
  private String sizeColumn;
  private String timeColumn;
  private String totalSizeColumn;
  private String totalTimeColumn;
  private String sizeUnit;
  private String timeUnit;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    builder.define("size_column", TokenType.COLUMN_NAME);
    builder.define("time_column", TokenType.COLUMN_NAME);
    builder.define("total_size_column", TokenType.COLUMN_NAME);
    builder.define("total_time_column", TokenType.COLUMN_NAME);
    builder.define("size_unit", TokenType.TEXT, Optional.TRUE);
    builder.define("time_unit", TokenType.TEXT, Optional.TRUE);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumn = ((ColumnName) args.value("size_column")).value();
    this.timeColumn = ((ColumnName) args.value("time_column")).value();
    this.totalSizeColumn = ((ColumnName) args.value("total_size_column")).value();
    this.totalTimeColumn = ((ColumnName) args.value("total_time_column")).value();
    
    if (args.contains("size_unit")) {
      this.sizeUnit = ((Text) args.value("size_unit")).value();
    } else {
      this.sizeUnit = "MB"; // Default to megabytes
    }
    
    if (args.contains("time_unit")) {
      this.timeUnit = ((Text) args.value("time_unit")).value();
    } else {
      this.timeUnit = "s"; // Default to seconds
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) 
    throws DirectiveExecutionException, ErrorRowException {
    if (rows == null || rows.isEmpty()) {
      return rows;
    }

    long totalBytes = 0;
    long totalMilliseconds = 0;

    // Accumulate totals
    for (Row row : rows) {
      Object sizeObj = row.getValue(sizeColumn);
      Object timeObj = row.getValue(timeColumn);

      if (sizeObj != null) {
        try {
          totalBytes += new ByteSize(sizeObj.toString()).getBytes();
        } catch (IllegalArgumentException e) {
          throw new DirectiveExecutionException(
            String.format("Invalid byte size format in column '%s': %s", sizeColumn, sizeObj));
        }
      }

      if (timeObj != null) {
        try {
          totalMilliseconds += new TimeDuration(timeObj.toString()).toMilliseconds();
        } catch (IllegalArgumentException e) {
          throw new DirectiveExecutionException(
            String.format("Invalid time duration format in column '%s': %s", timeColumn, timeObj));
        }
      }
    }

    // Convert totals to requested units
    double convertedSize = convertBytes(totalBytes, sizeUnit);
    double convertedTime = convertMilliseconds(totalMilliseconds, timeUnit);

    // Create a new row with the aggregated results
    Row result = new Row();
    result.add(totalSizeColumn, convertedSize);
    result.add(totalTimeColumn, convertedTime);

    return List.of(result);
  }

  private double convertBytes(long bytes, String targetUnit) {
    double result;
    switch (targetUnit.toUpperCase()) {
      case "B":
        result = bytes;
        break;
      case "KB":
        result = bytes / 1024.0;
        break;
      case "MB":
        result = bytes / (1024.0 * 1024.0);
        break;
      case "GB":
        result = bytes / (1024.0 * 1024.0 * 1024.0);
        break;
      case "TB":
        result = bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
        break;
      default:
        throw new IllegalArgumentException("Unknown byte unit: " + targetUnit);
    }
    return result;
  }

  private double convertMilliseconds(long milliseconds, String targetUnit) {
    double result;
    switch (targetUnit) {
      case "ms":
        result = milliseconds;
        break;
      case "s":
        result = milliseconds / 1000.0;
        break;
      case "m":
        result = milliseconds / (60.0 * 1000.0);
        break;
      case "h":
        result = milliseconds / (60.0 * 60.0 * 1000.0);
        break;
      case "d":
        result = milliseconds / (24.0 * 60.0 * 60.0 * 1000.0);
        break;
      default:
        throw new IllegalArgumentException("Unknown time unit: " + targetUnit);
    }
    return result;
  }

  @Override
  public void destroy() {
    // No resources to clean up
  }
}
