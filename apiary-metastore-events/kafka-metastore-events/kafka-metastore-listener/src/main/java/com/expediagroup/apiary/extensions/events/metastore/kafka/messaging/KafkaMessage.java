/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.apiary.extensions.events.metastore.kafka.messaging;

import static com.expediagroup.apiary.extensions.events.metastore.common.Preconditions.checkNotEmpty;
import static com.expediagroup.apiary.extensions.events.metastore.common.Preconditions.checkNotNull;

public class KafkaMessage {

  public static class Builder {
    private String database;
    private String table;
    private long timestamp = System.currentTimeMillis();
    private byte[] payload;

    private Builder() {}

    public Builder database(String database) {
      this.database = database;
      return this;
    }

    public Builder table(String table) {
      this.table = table;
      return this;
    }

    public Builder timestamp(long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder payload(byte[] payload) {
      this.payload = payload;
      return this;
    }

    public KafkaMessage build() {
      return new KafkaMessage(checkNotEmpty(database, "Parameter 'database' is required").trim(),
          checkNotEmpty(table, "Parameter 'table' is required").trim(), timestamp,
          checkNotNull(payload, "Parameter 'payload' is required"));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final String database;
  private final String table;
  private final long timestamp;
  private final byte[] payload;

  private KafkaMessage(String database, String table, long timestamp, byte[] payload) {
    this.database = database;
    this.table = table;
    this.timestamp = timestamp;
    this.payload = payload;
  }

  public String getQualifiedTableName() {
    return String.format("%s.%s", database, table);
  }

  public String getDatabase() {
    return database;
  }

  public String getTable() {
    return table;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public byte[] getPayload() {
    return payload;
  }

}
