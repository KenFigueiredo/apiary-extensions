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

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.google.common.base.Preconditions.checkNotNull;

import static com.hotels.shunting.yard.common.PropertyUtils.stringProperty;
import static com.hotels.shunting.yard.receiver.kafka.KafkaConsumerProperty.TOPIC;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.expediagroup.apiary.extensions.events.receiver.common.error.SerDeException;
import com.expediagroup.apiary.extensions.events.receiver.common.event.ListenerEvent;
import com.expediagroup.apiary.extensions.events.receiver.common.event.serializable.SerializableListenerEvent;
import com.expediagroup.apiary.extensions.events.receiver.common.messaging.JsonMetaStoreEventDeserializer;
import com.expediagroup.apiary.extensions.events.receiver.common.messaging.MessageEvent;
import com.expediagroup.apiary.extensions.events.receiver.common.messaging.MessageProperty;
import com.expediagroup.apiary.extensions.events.receiver.common.messaging.MessageReader;
import com.expediagroup.apiary.extensions.events.receiver.common.messaging.MetaStoreEventDeserializer;
import com.expediagroup.apiary.extensions.events.receiver.sqs.messaging.SqsMessageProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaMessageReader implements MessageReader {

  private final String bootstrapServers;
  private String topic;
  private final String groupId;
  private final MetaStoreEventDeserializer metaStoreEventDeserializer;
  private final KafkaConsumer<String, byte[]> consumer;
  private Iterator<ConsumerRecord<String, byte[]>> records;

  // public KafkaMessageReader(MetaStoreEventDeserializer metaStoreEventDeserializer) {
  // this(metaStoreEventDeserializer, new HiveMetaStoreEventKafkaConsumer());
  // }

  private KafkaMessageReader(
      String bootstrapServers,
      String topic,
      String groupId,
      MetaStoreEventDeserializer metaStoreEventDeserializer,
      KafkaConsumer<String, byte[]> consumer) {
    this.bootstrapServers = bootstrapServers;
    this.topic = topic;
    this.groupId = groupId;
    this.metaStoreEventDeserializer = metaStoreEventDeserializer;
    this.consumer = consumer;
  }

  // KafkaMessageReader(MetaStoreEventDeserializer metaStoreEventDeserializer, KafkaConsumer<Long, byte[]> consumer) {
  // this.consumer = consumer;
  // this.metaStoreEventDeserializer = metaStoreEventDeserializer;
  // init();
  // }

  private void init() {
    topic = checkNotNull(stringProperty(conf, TOPIC), "Property " + TOPIC + " is not set");
    consumer.subscribe(Arrays.asList(topic));
  }

  @Override
  public void close() throws IOException {
    consumer.close();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove message from Kafka topic");
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public SerializableListenerEvent next() {
    readRecordsIfNeeded();
    return eventPayLoad(records.next());
  }

  private void readRecordsIfNeeded() {
    while (records == null || !records.hasNext()) {
      records = consumer.poll(Long.MAX_VALUE).iterator();
    }
  }

  private SerializableListenerEvent eventPayLoad(ConsumerRecord<Long, byte[]> message) {
    try {
      return metaStoreEventDeserializer.unmarshal(new String(message.value()));
    } catch (Exception e) {
      throw new SerDeException("Unable to unmarshall event", e);
    }
  }

  @Override
  public Optional<MessageEvent> read() {
    if (records == null || !records.hasNext()) {
      records = consumer.poll(Long.MAX_VALUE).iterator();
    }
    return null;

  }

  private Iterator<Message> receiveMessage() {
    ReceiveMessageRequest request = new ReceiveMessageRequest()
        .withQueueUrl(queueUrl)
        .withWaitTimeSeconds(waitTimeSeconds)
        .withMaxNumberOfMessages(maxMessages);
    return consumer.receiveMessage(request).getMessages().iterator();
  }

  private MessageEvent messageEvent(Message message) {
    ListenerEvent listenerEvent = eventPayLoad(message);
    Map<MessageProperty, String> properties = Collections
        .singletonMap(SqsMessageProperty.SQS_MESSAGE_RECEIPT_HANDLE, message.getReceiptHandle());
    return new MessageEvent(listenerEvent, properties);
  }

  private ListenerEvent eventPayLoad(Message message) {
    try {
      return messageDeserializer.unmarshal(message.getBody());
    } catch (Exception e) {
      throw new SerDeException("Unable to unmarshall event", e);
    }
  }

  @Override
  public void delete(MessageEvent messageEvent) {

  }

  public static final class Builder {
    private final String bootstrapServers;
    private List<String> topic;
    private String groupId;
    private KafkaConsumer<String, byte[]> consumer;
    private MetaStoreEventDeserializer metaStoreEventDeserializer;

    public Builder(String bootstrapServers) {
      this.bootstrapServers = bootstrapServers;
    }

    public Builder withTopic(List<String> topic) {
      this.topic = topic;
      return this;
    }

    public Builder withGroupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder withConsumer(KafkaConsumer<String, byte[]> consumer) {
      this.consumer = consumer;
      return this;
    }

    public Builder withMetaStoreEventDeserializer(MetaStoreEventDeserializer metaStoreEventDeserializer) {
      this.metaStoreEventDeserializer = metaStoreEventDeserializer;
      return this;
    }

    public KafkaMessageReader build() {
      checkNotNull(bootstrapServers);
      checkNotNull(groupId);
      if (topic.isEmpty()) {
        throw new IllegalArgumentException("Topic cannot be empty.");
      }

      consumer = (consumer == null) ? defaultConsumer(bootstrapServers, topic, groupId) : consumer;
      metaStoreEventDeserializer = (metaStoreEventDeserializer == null) ? defaultMetaStoreEventDeserializer()
          : metaStoreEventDeserializer;
      // maxMessages = (maxMessages == null) ? DEFAULT_MAX_MESSAGES : maxMessages;
      // waitTimeSeconds = (waitTimeSeconds == null) ? DEFAULT_POLLING_WAIT_TIME_SECONDS : waitTimeSeconds;

      return new KafkaMessageReader(bootstrapServers, topic, groupId, metaStoreEventDeserializer, consumer);
    }

    private KafkaConsumer<String, byte[]> defaultConsumer(String bootstrapServers, List<String> topic, String groupId) {
      KafkaConsumer<String, byte[]> consumer = new HiveMetaStoreEventKafkaConsumer(bootstrapServers, groupId);
      consumer.subscribe(topic);
      return consumer;
    }

    private MetaStoreEventDeserializer defaultMetaStoreEventDeserializer() {
      ObjectMapper mapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
      return new JsonMetaStoreEventDeserializer(mapper);
    }

  }

}
