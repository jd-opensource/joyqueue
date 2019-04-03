package com.jd.journalq.client;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.message.Message;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class ConsumerTest1 extends AbstractConsumerTest {

    public static final String TOPIC = "test_consume_1";

    public static final String BROADCAST_TOPIC = "test_consume_broadcast_1";

    @Before
    @Override
    public void before() {
        super.before();

        new ProduceDataTest1(new String[] {TOPIC, BROADCAST_TOPIC}).testProduce();
    }

    @Test
    public void testGetQueueMetaData() {
        QueueMetaData queueMetaData = consumer.getQueueMetaData(TOPIC);
        Assert.assertNotNull(queueMetaData);
        Assert.assertEquals(queueMetaData.queueName(), TOPIC);
        Assert.assertEquals(queueMetaData.partitions().size(), 10);
        for (int i = 0; i < 10; i++) {
            QueueMetaData.Partition partition = queueMetaData.partitions().get(i);
            Assert.assertEquals(partition.partitionId(), i);
            Assert.assertEquals(partition.partitonHost(), IpUtil.getLocalIp() + ":50088");
        }
    }

    @Test
    public void testBindQueue() throws Exception {
        consumer.bindQueue(TOPIC, new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                Assert.assertEquals(message.header().getDestination(), TOPIC);
                logger.info("onReceived message, " + message);
                context.ack();
            }
        });
        Assert.assertEquals(true, consumer.isBindQueue());

        Thread.currentThread().sleep(1000 * 5);

        consumer.suspend();
        Assert.assertEquals(true, consumer.isSuspended());

        consumer.resume();
        Assert.assertEquals(false, consumer.isSuspended());

        consumer.unbindQueue(TOPIC);
        Assert.assertEquals(false, consumer.isBindQueue());
    }

    @Test
    public void testInterceptor() throws Exception {
        int[] preReceive = {0};
        int[] postReceive = {0};
        int[] onReceived = {0};

        ConsumerInterceptor consumerInterceptor = new ConsumerInterceptor() {
            @Override
            public void preReceive(Message message, Context context) {
                context.attributes().put("property_1", "property_1_value");
                preReceive[0]++;
            }

            @Override
            public void postReceive(Message message, Context context) {
                Assert.assertEquals("property_1_value", context.attributes().getString("property_1"));
                postReceive[0]++;
            }
        };

        consumer.addInterceptor(consumerInterceptor);
        consumer.bindQueue(TOPIC, new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                logger.info("onReceived message, " + message);
                onReceived[0]++;
                context.ack();
            }
        });

        if (onReceived[0] != 0) {
            Assert.assertEquals(onReceived[0], preReceive[0]);
            Assert.assertEquals(onReceived[0], postReceive[0]);
        }

        consumer.removeInterceptor(consumerInterceptor);
        consumer.unbindQueue(TOPIC);
    }

    @Test
    public void testBroadcastQueue() throws Exception {
        consumer.bindQueue(BROADCAST_TOPIC, new BatchMessageListener() {
            @Override
            public void onReceived(List<Message> list, Context context) {
                for (Message message : list) {
                    Assert.assertEquals(BROADCAST_TOPIC, message.header().getDestination());
                }
                logger.info("onReceived broadcast message, " + list);
                context.ack();
            }
        });

        Thread.currentThread().sleep(1000 * 5);
    }

    @Test
    public void testReceive() {
        consumer.bindQueue(TOPIC);
        Message message = consumer.receive(1000 * 1);

        if (message == null) {
            logger.info("no message");
            return;
        }

        Assert.assertEquals(TOPIC, message.header().getDestination());

        consumer.ack(message.getMessageReceipt());
    }

    @Test
    public void testBatchReceive() {
        consumer.bindQueue(TOPIC);
        List<Message> messages = consumer.batchReceive(1000 * 1);

        if (CollectionUtils.isEmpty(messages)) {
            logger.info("no messages");
            return;
        }

        logger.info("batchReceive {} messages", messages.size());

        for (Message message : messages) {
            Assert.assertEquals(TOPIC, message.header().getDestination());
            consumer.ack(message.getMessageReceipt());
        }
    }

    @Test
    public void testPartitionReceive() {
        consumer.bindQueue(TOPIC);
        Message message = consumer.receive((short) 0, 1000 * 1);

        if (message == null) {
            logger.info("no message");
            return;
        }

        Assert.assertEquals(TOPIC, message.header().getDestination());

        consumer.ack(message.getMessageReceipt());
    }

    @Test
    public void testBatchPartitionReceive() {
        consumer.bindQueue(TOPIC);
        List<Message> messages = consumer.batchReceive((short) 0,1000 * 1);

        if (CollectionUtils.isEmpty(messages)) {
            logger.info("no messages");
            return;
        }

        logger.info("batchReceive {} messages", messages.size());

        for (Message message : messages) {
            Assert.assertEquals(TOPIC, message.header().getDestination());
            consumer.ack(message.getMessageReceipt());
        }
    }

    @Test
    public void testPartitionIndexReceive() {
        consumer.bindQueue(TOPIC);
        Message message = consumer.receive((short) 0, 0, 1000 * 1);

        if (message == null) {
            logger.info("no message");
            return;
        }

        Assert.assertEquals(TOPIC, message.header().getDestination());

        consumer.ack(message.getMessageReceipt());
    }

    @Test
    public void testBatchPartitionIndexReceive() {
        consumer.bindQueue(TOPIC);
        List<Message> messages = consumer.batchReceive((short) 0, 0,1000 * 1);

        if (CollectionUtils.isEmpty(messages)) {
            logger.info("no messages");
            return;
        }

        logger.info("batchReceive {} messages", messages.size());

        for (Message message : messages) {
            Assert.assertEquals(TOPIC, message.header().getDestination());
            consumer.ack(message.getMessageReceipt());
        }
    }
}