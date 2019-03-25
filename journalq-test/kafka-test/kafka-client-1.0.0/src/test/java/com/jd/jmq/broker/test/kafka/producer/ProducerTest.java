package com.jd.journalq.broker.test.kafka.producer;

import com.jd.journalq.broker.test.kafka.common.KafkaConfigs;
import com.jd.journalq.broker.test.kafka.common.ProducerTestCommon;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ProducerTest  extends ProducerTestCommon {
    @Before
    public void setUp() {
        super.setUp();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    /**
     * Test acks config
     */
    @Test
    public void testAck() {
        super.testAck();
    }

    @Test
    public void testCompress() {
        super.testCompress();
    }

    @Test
    public void testSerializer() {
        super.testSerializer();
    }

    @Test
    public void testQueryOffsetByTime() {
        super.testQueryOffsetByTime();
    }

    @Test
    public void testProduceRecord() {
        super.testProduceRecord();
    }

    @Test
    public void testAutoOffsetReset() {
        super.testAutoOffsetReset();
    }

    @Test
    public void testConsumeMaxBytes() {
        super.testConsumeMaxBytes();
    }

    @Test
    public void testConsumeMaxRecords() {
        super.testConsumeMaxRecords();
    }

}
