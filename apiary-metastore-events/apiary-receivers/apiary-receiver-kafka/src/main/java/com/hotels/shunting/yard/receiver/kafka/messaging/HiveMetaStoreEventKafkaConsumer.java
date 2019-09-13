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
package com.hotels.shunting.yard.receiver.kafka.messaging;

import static com.google.common.base.Preconditions.checkNotNull;

import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.AUTO_COMMIT_INTERVAL_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.BOOTSTRAP_SERVERS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.CLIENT_ID;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.CONNECTIONS_MAX_IDLE_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.ENABLE_AUTO_COMMIT;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.FETCH_MAX_BYTES;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.GROUP_ID;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.KEY_SERIALIZER;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.MAX_POLL_INTERVAL_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.MAX_POLL_RECORDS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.RECEIVE_BUFFER_BYTES;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.RECONNECT_BACKOFF_MAX_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.RECONNECT_BACKOFF_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.RETRY_BACKOFF_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.SESSION_TIMEOUT_MS;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.VALUE_SERIALIZER;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.google.common.annotations.VisibleForTesting;

class HiveMetaStoreEventKafkaConsumer extends KafkaConsumer<String, byte[]> {

  public HiveMetaStoreEventKafkaConsumer(String bootstrapServers, String groupId) {
    super(kafkaProperties(bootstrapServers, groupId));
  }

  @VisibleForTesting
  static Properties kafkaProperties(String bootstrapServers, String groupId) {
    Properties props = new Properties();
    props
        .put(BOOTSTRAP_SERVERS.unPrefixedKey(),
            checkNotNull(bootstrapServers, "Property " + BOOTSTRAP_SERVERS + " is not set"));
    props.put(GROUP_ID.unPrefixedKey(), checkNotNull(groupId, "Property " + GROUP_ID + " is not set"));
    props.put(CLIENT_ID.unPrefixedKey(), CLIENT_ID.defaultValue());

    props.put(SESSION_TIMEOUT_MS.unPrefixedKey(), SESSION_TIMEOUT_MS.defaultValue());
    props.put(CONNECTIONS_MAX_IDLE_MS.unPrefixedKey(), CONNECTIONS_MAX_IDLE_MS.defaultValue());
    props.put(RECONNECT_BACKOFF_MAX_MS.unPrefixedKey(), RECONNECT_BACKOFF_MAX_MS.defaultValue());
    props.put(RECONNECT_BACKOFF_MS.unPrefixedKey(), RECONNECT_BACKOFF_MS.defaultValue());
    props.put(RETRY_BACKOFF_MS.unPrefixedKey(), RETRY_BACKOFF_MS.defaultValue());
    props.put(MAX_POLL_INTERVAL_MS.unPrefixedKey(), MAX_POLL_INTERVAL_MS.defaultValue());
    props.put(MAX_POLL_RECORDS.unPrefixedKey(), MAX_POLL_RECORDS.defaultValue());
    props.put(ENABLE_AUTO_COMMIT.unPrefixedKey(), ENABLE_AUTO_COMMIT.defaultValue());
    props.put(AUTO_COMMIT_INTERVAL_MS.unPrefixedKey(), AUTO_COMMIT_INTERVAL_MS.defaultValue());
    props.put(FETCH_MAX_BYTES.unPrefixedKey(), FETCH_MAX_BYTES.defaultValue());
    props.put(RECEIVE_BUFFER_BYTES.unPrefixedKey(), RECEIVE_BUFFER_BYTES.defaultValue());
    props.put(KEY_SERIALIZER, "org.apache.kafka.common.serialization.LongSerializer");
    props.put(VALUE_SERIALIZER, "org.apache.kafka.common.serialization.ByteArraySerializer");
    return props;
  }

}
