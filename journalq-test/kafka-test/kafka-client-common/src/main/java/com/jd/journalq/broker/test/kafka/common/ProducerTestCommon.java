/**
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
package com.jd.journalq.broker.test.kafka.common;

import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.CompressionType;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class ProducerTestCommon {
    private static Logger logger = LoggerFactory.getLogger(ProducerTestCommon.class);

    private final int size = 100;

    private final int partition = 1;

    private String[] stringKeys = new String[size];
    private String[] stringValues = new String[size];

    public void setUp() {
        for (int i = 0; i < size; i++) {
            stringKeys[i] = "test_key" + i;
            stringValues[i] = "test_value" + i;
        }
    }

    public void tearDown() {

    }

    /**
     * Test acks config
     */
    public void testAck() {
        logger.info("Test acks 0 ========================");
        try (KafkaProducer<String, String> producer = createProducerWithAcks("0")) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

        logger.info("Test acks 1 ========================");
        try (KafkaProducer<String, String> producer = createProducerWithAcks("1")) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

        logger.info("Test acks -1 =======================");
        try (KafkaProducer<String, String> producer = createProducerWithAcks("-1")) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

        logger.info("Test acks all ======================");
        try (KafkaProducer<String, String> producer = createProducerWithAcks("all")) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

    }

    private KafkaProducer<String, String> createProducerWithAcks(String acks) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaConfigs.BOOTSTRAP);
        props.put("client.id", KafkaConfigs.PRODUCE_CLIENT_ID);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", acks);

        return new KafkaProducer<>(props);

    }

    private <K, V> void produceRecords(KafkaProducer<K, V> producer, Integer partition, K[] keys, V[] values) {
        try {
            for (int i = 0; i < size; i++) {
                if (partition != null) {
                    producer.send(new ProducerRecord<>(KafkaConfigs.TOPIC, partition, keys[i], values[i])).get();
                } else if (keys != null) {
                    producer.send(new ProducerRecord<>(KafkaConfigs.TOPIC, keys[i], values[i])).get();
                } else {
                    producer.send(new ProducerRecord<>(KafkaConfigs.TOPIC, values[i])).get();
                }
                logger.info("Produce record:{partition:{}, key:{}, value:{}}",
                        partition, keys == null ? null : keys[i], values[i]);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
        }
    }

    /**
     * Produce records, partition is null, key is null
     *
     * @param producer producer
     */
    private <K, V> void produceRecords(KafkaProducer<K, V> producer, V[] values) {
        produceRecords(producer, null, null, values);
    }

    /**
     * Produce records, partition is null
     *
     * @param producer producer
     * @param keys     keys
     */
    private <K, V> void produceRecords(KafkaProducer<K, V> producer, K[] keys, V[] values) {
        produceRecords(producer, null, keys, values);
    }


    private <K, V> KafkaConsumer<K, V> createConsumer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        return new KafkaConsumer<>(consumerProps);
    }

    private <K, V> List<ConsumerRecord<K, V>> consumeRecords(KafkaConsumer<K, V> consumer) {
        consumer.subscribe(Collections.singletonList(KafkaConfigs.TOPIC));
        List<ConsumerRecord<K, V>> consumeRecords = new LinkedList<>();
        int i = 0;
        try {
            ConsumerRecords<K, V> records;
            do {
                records = consumer.poll(1000);
                for (ConsumerRecord<K, V> record : records) {
                    logger.info("consumer message partition:{}, key:{}, value:{}",
                            record.partition(), record.key(), record.value());
                    consumeRecords.add(record);
                }
                i++;
                Thread.sleep(10);
            } while (i < 10 || !records.isEmpty());
            Thread.sleep(2000); //wait commit offset finished
            return consumeRecords;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail();
            return consumeRecords;
        }
    }

    private <K, V> List<ConsumerRecord<K, V>> consumeRecords() {
        try (KafkaConsumer<K, V> consumer = createConsumer()) {
            return consumeRecords(consumer);
        }
    }

    private <K, V> void assertConsumeRecords(List<ConsumerRecord<K, V>> consumerRecords, Integer partition, K[] keys, V[] values) {
        Assert.assertNotNull(consumerRecords);
        Assert.assertEquals(consumerRecords.size(), size);
        for (int i = 0; i < size; i++) {
            ConsumerRecord<K, V> record = consumerRecords.get(i);

            logger.info("consumer position:{}, key:{}, value:{}, expect partition:{}, key:{}, value:{}",
                    record.partition(), record.key(), record.value(), partition, keys == null ? null : keys[i], values[i]);
            if (partition != null) {
                Assert.assertEquals(record.partition(), (int) partition);
            }
            if (keys != null) {
                Assert.assertEquals(record.key(), keys[i]);
            }
            Assert.assertEquals(record.value(), values[i]);
        }
    }

    private <K, V> void assertConsumeRecords(List<ConsumerRecord<K, V>> consumerRecords, V[] values) {
        assertConsumeRecords(consumerRecords, null, null, values);
    }

    private <K, V> void assertConsumeRecords(List<ConsumerRecord<K, V>> consumerRecords, K[] keys, V[] values) {
        assertConsumeRecords(consumerRecords, null, keys, values);
    }


    /**
     * Test compress type
     */
    public void testCompress() {
        logger.info("Test compress gzip   ========================");
        try (KafkaProducer<String, String> producer = createProducerWithCompress(CompressionType.GZIP)) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

        logger.info("Test compress snappy ========================");
        try (KafkaProducer<String, String> producer = createProducerWithCompress(CompressionType.SNAPPY)) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

        logger.info("Test compress lz4    ========================");
        try (KafkaProducer<String, String> producer = createProducerWithCompress(CompressionType.LZ4)) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

        logger.info("Test compress none   ========================");
        try (KafkaProducer<String, String> producer = createProducerWithCompress(CompressionType.NONE)) {
            produceRecords(producer, partition, stringKeys, stringValues);
            assertConsumeRecords(consumeRecords(), stringKeys, stringValues);
        }

    }

    private KafkaProducer<String, String> createProducerWithCompress(CompressionType compressionType) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaConfigs.BOOTSTRAP);
        props.put("client.id", KafkaConfigs.PRODUCE_CLIENT_ID);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType.name);

        return new KafkaProducer<>(props);

    }


    public void testSerializer() {
        // String serilization
        logger.info("Test serializer String    ========================");
        try (KafkaProducer<String, String> producer = createProducerWithSerializer("org.apache.kafka.common.serialization.StringSerializer",
                "org.apache.kafka.common.serialization.StringSerializer")) {
            produceRecords(producer, partition, stringKeys, stringValues);
        }

        List<ConsumerRecord<String, String>> consumerRecords = consumeRecords();
        assertConsumeRecords(consumerRecords, stringKeys, stringValues);

        // Byte Array serilization
        logger.info("Test serializer byte array ========================");
        byte[][] byteArrayKeys = new byte[size][1];
        byte[][] byteArrayValues = new byte[size][1];
        for (int i = 0; i < size; i++) {
            byteArrayKeys[i][0] = (byte) i;
            byteArrayValues[i][0] = (byte) (i + 1);
        }
        try (KafkaProducer<byte[], byte[]> byteArrayProducer = createProducerWithSerializer("org.apache.kafka.common.serialization.ByteArraySerializer",
                "org.apache.kafka.common.serialization.ByteArraySerializer")) {
            produceRecords(byteArrayProducer, partition, byteArrayKeys, byteArrayValues);
        }

        try (KafkaConsumer<byte[], byte[]> byteArrayConsumer = createConsumerWithDeserializer("org.apache.kafka.common.serialization.ByteArrayDeserializer",
                "org.apache.kafka.common.serialization.ByteArrayDeserializer")) {
            List<ConsumerRecord<byte[], byte[]>> consumerByteArrayRecords = consumeRecords(byteArrayConsumer);
            //assertConsumeRecords(consumerByteArrayRecords, byteArrayKeys, byteArrayValues);
        }

        // ByteBuffer serilization
        logger.info("Test serializer byte buffer ========================");
        ByteBuffer[] byteBufferKeys = new ByteBuffer[size];
        ByteBuffer[] byteBufferValues = new ByteBuffer[size];
        for (int i = 0; i < size; i++) {
            byteBufferKeys[i] = ByteBuffer.allocate(10);
            byteBufferKeys[i].putInt(i);
            byteBufferKeys[i].rewind();

            byteBufferValues[i] = ByteBuffer.allocate(10);
            byteBufferValues[i].putInt(i + 1);
            byteBufferValues[i].rewind();
        }
        try (KafkaProducer<ByteBuffer, ByteBuffer> byteBufferProducer = createProducerWithSerializer("org.apache.kafka.common.serialization.ByteBufferSerializer",
                "org.apache.kafka.common.serialization.ByteBufferSerializer")) {
            produceRecords(byteBufferProducer, partition, byteBufferKeys, byteBufferValues);
        }

        try (KafkaConsumer<ByteBuffer, ByteBuffer> byteBufferConsumer = createConsumerWithDeserializer("org.apache.kafka.common.serialization.ByteBufferDeserializer",
                "org.apache.kafka.common.serialization.ByteBufferDeserializer")) {
            List<ConsumerRecord<ByteBuffer, ByteBuffer>> consumerByteBufferRecords = consumeRecords(byteBufferConsumer);
            assertConsumeRecords(consumerByteBufferRecords, byteBufferKeys, byteBufferValues);
        }

        // Long serilization
        logger.info("Test serializer Long ========================");
        Long[] longKeys = new Long[size];
        Long[] longValues = new Long[size];
        for (int i = 0; i < size; i++) {
            longKeys[i] = i + 1L;
            longValues[i] = i + 2L;
        }
        try (KafkaProducer<Long, Long> longProducer = createProducerWithSerializer("org.apache.kafka.common.serialization.LongSerializer",
                "org.apache.kafka.common.serialization.LongSerializer")) {
            produceRecords(longProducer, partition, longKeys, longValues);
        }

        try (KafkaConsumer<Long, Long> longConsumer = createConsumerWithDeserializer("org.apache.kafka.common.serialization.LongDeserializer",
                "org.apache.kafka.common.serialization.LongDeserializer")) {
            List<ConsumerRecord<Long, Long>> consumerLongRecords = consumeRecords(longConsumer);
            assertConsumeRecords(consumerLongRecords, longKeys, longValues);
        }
    }


    private <K, V> KafkaConsumer<K, V> createConsumerWithDeserializer(String keyDeserializer, String valueDeserializer) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

        return new KafkaConsumer<>(consumerProps);
    }


    private <K, V> KafkaProducer<K, V> createProducerWithSerializer(String keySerializer, String valueSerializer) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaConfigs.BOOTSTRAP);
        props.put("client.id", KafkaConfigs.PRODUCE_CLIENT_ID);
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);

        return new KafkaProducer<>(props);

    }


    public void testQueryOffsetByTime() {
        KafkaProducer<String, String> producer = createProducerWithAcks("1");
        produceRecords(producer, partition, stringKeys, stringValues);

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException ignored) {
        }

        produceRecords(producer, partition, stringKeys, stringValues);

        KafkaConsumer<String, String> consumer = createConsumer();
        TopicPartition topicPartition = new TopicPartition(KafkaConfigs.TOPIC, partition);

        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        timestampsToSearch.put(topicPartition, SystemClock.now() - 9 * 1000);
        Map<TopicPartition, OffsetAndTimestamp> offsetAndTimestamps = consumer.offsetsForTimes(timestampsToSearch);

        for (TopicPartition tp : offsetAndTimestamps.keySet()) {
            consumer.seek(topicPartition, offsetAndTimestamps.get(tp).offset());
            List<ConsumerRecord<String, String>> consumerRecords = consumeRecords(consumer);
            assertConsumeRecords(consumerRecords, partition, stringKeys, stringValues);
        }
    }


    public void testProduceRecord() {
        KafkaProducer<String, String> producer = createProducerWithAcks("1");
        final int partition = 1;

        logger.info("Test produce record partition ========================");
        produceRecords(producer, partition, stringKeys, stringValues);
        assertConsumeRecords(consumeRecords(), partition, stringKeys, stringValues);

        logger.info("Test produce record key value ========================");
        produceRecords(producer, stringKeys, stringValues);
        //assertConsumeRecords(consumeRecords(), stringKeys, stringValues);

        logger.info("Test produce record value     ========================");
        produceRecords(producer, stringValues);
        //assertConsumeRecords(consumeRecords(), stringValues);

        consumeRecords();
    }


    protected void testAutoOffsetReset() {
        logger.info("Test auto.offset.reset earliest ========================");
        KafkaProducer<String, String> producer = createProducerWithAcks("1");
        produceRecords(producer, partition, stringKeys, stringValues);

        try (KafkaConsumer<String, String> consumer = createConsumerWithAutoOffsetReset("earliest")) {
            List<ConsumerRecord<String, String>> consumerRecords = consumeRecords(consumer);
            assertConsumeRecords(consumerRecords, stringKeys, stringValues);
        }

        try {
            logger.info("Please delete the consume position cache and file");
            Thread.sleep(30 * 1000);
        } catch (InterruptedException ignored) {
        }

        // 手动删除offset

        logger.info("Test auto.offset.reset latest ========================");
        produceRecords(producer, partition, stringKeys, stringValues);

        KafkaConsumer<String, String> consumer = createConsumerWithAutoOffsetReset("latest");
        List<ConsumerRecord<String, String>> consumerRecords = consumeRecords(consumer);
        Assert.assertEquals(consumerRecords.size(), 0);

        produceRecords(producer, partition, stringKeys, stringValues);

        consumerRecords = consumeRecords(consumer);
        assertConsumeRecords(consumerRecords, stringKeys, stringValues);

    }

    private <K, V> KafkaConsumer<K, V> createConsumerWithAutoOffsetReset(String autoOffsetReset) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaConsumer<>(consumerProps);
    }


    protected void testConsumeMaxBytes() {
        try (KafkaProducer<String, String> producer = createProducerWithAcks("1")) {
            produceRecords(producer, partition, stringKeys, stringValues);
        }

        int maxBytes = 100;
        try (KafkaConsumer<String, String> consumer = createConsumerWithConfig(
                new String[]{"fetch.max.bytes", "max.partition.fetch.bytes"}, new String[]{String.valueOf(maxBytes), String.valueOf(maxBytes)})) {
            consumer.subscribe(Collections.singletonList(KafkaConfigs.TOPIC));
            int i = 0;
            try {
                ConsumerRecords<String, String> records;
                do {
                    records = consumer.poll(1000);
                    i++;
                } while (i < 10 && records.isEmpty());

                int length = 0;
                for (ConsumerRecord<String, String> record : records) {
                    length += (record.key().getBytes().length + record.value().getBytes().length);
                }

                logger.info("Consume {} bytes records", length);
                Assert.assertTrue(length <= maxBytes);

            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                Assert.fail();
            }
        }

        consumeRecords();

        try (KafkaConsumer<String, String> consumer = createConsumerWithConfig(
                new String[]{"fetch.max.bytes"}, new String[]{String.valueOf(1)})) {
            consumer.subscribe(Collections.singletonList(KafkaConfigs.TOPIC));
            try (KafkaProducer<String, String> producer = createProducerWithAcks("1")) {
                produceRecords(producer, partition, stringKeys, stringValues);
            }
            List<ConsumerRecord<String, String>> consumerRecords = consumeRecords(consumer);
            assertConsumeRecords(consumerRecords, partition, stringKeys, stringValues);
        }

    }

    protected void testConsumeMaxRecords() {
        try (KafkaProducer<String, String> producer = createProducerWithAcks("1")) {
            produceRecords(producer, partition, stringKeys, stringValues);
        }

        try (KafkaConsumer<String, String> consumer = createConsumerWithConfig(
                new String[]{"max.poll.records"}, new String[]{"10"})) {
            consumer.subscribe(Collections.singletonList(KafkaConfigs.TOPIC));
            int i = 0;
            try {
                ConsumerRecords<String, String> records;
                do {
                    records = consumer.poll(1000);
                    i++;
                } while (i < 10 || records.isEmpty());

                logger.info("Consume {} records", records.count());
                Assert.assertTrue(records.count() <= 10);
            } catch (Throwable t) {
                Assert.fail();
            }
        }

    }

    private <K, V> KafkaConsumer<K, V> createConsumerWithConfig(String[] configItems, String[] configValues) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConfigs.GROUP_ID);
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        for (int i = 0; i < configItems.length; i++) {
            consumerProps.put(configItems[i], configValues[i]);
        }

        return new KafkaConsumer<>(consumerProps);
    }

}
