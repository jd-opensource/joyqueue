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
package com.jd.journalq.client;

import com.google.common.collect.Lists;
import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.Context;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.journalq.domain.JMQProducerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQTxFeedbackBuiltinKeys;
import io.openmessaging.journalq.producer.ExtensionProducer;
import io.openmessaging.journalq.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;
import io.openmessaging.producer.TransactionStateCheckListener;
import io.openmessaging.producer.TransactionalResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/12
 */
public class ProducerTest1 extends AbstractProducerTest {

    public static final String TOPIC = "test_produce_1";

    public static final String BROADCAST_TOPIC = "test_produce_broadcast_1";

    public static final String TRANSACTION_TOPIC = "test_produce_transaction_1";

    @Override
    protected KeyValue getAttributes() {
        KeyValue keyValue = super.getAttributes();
        keyValue.put(JMQTxFeedbackBuiltinKeys.FETCH_INTERVAL, 1);
        keyValue.put(JMQTxFeedbackBuiltinKeys.FETCH_SIZE, 10);
        keyValue.put(JMQProducerBuiltinKeys.TRANSACTION_TIMEOUT, 1000 * 10);
        return keyValue;
    }

    @Test
    public void testGetQueueMetaData() {
        QueueMetaData queueMetaData = producer.getQueueMetaData(TOPIC);
        Assert.assertNotNull(queueMetaData);
        Assert.assertEquals(TOPIC, queueMetaData.queueName());
        Assert.assertEquals(10, queueMetaData.partitions().size());
        for (int i = 0; i < 10; i++) {
            QueueMetaData.Partition partition = queueMetaData.partitions().get(i);
            Assert.assertEquals(i, partition.partitionId());
            Assert.assertEquals(IpUtil.getLocalIp() + ":50088", partition.partitonHost());
        }
    }

    @Test
    public void testBroadcastSend() {
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(BROADCAST_TOPIC, "test_body".getBytes());
            Assert.assertEquals(BROADCAST_TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));

            SendResult sendResult = producer.send(message);
            Assert.assertEquals(null, sendResult.messageId());
        }
    }

    @Test
    public void testSend() {
        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        Assert.assertEquals(TOPIC, message.header().getDestination());
        Assert.assertEquals("test_body", new String(message.getData()));

        SendResult sendResult = producer.send(message);
        Assert.assertEquals(null, sendResult.messageId());
    }

    @Test
    public void testSendAsync() {
        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        Assert.assertEquals(TOPIC, message.header().getDestination());
        Assert.assertEquals("test_body", new String(message.getData()));

        SendResult sendResult = producer.sendAsync(message).get();
        Assert.assertEquals(null, sendResult.messageId());
    }

    @Test
    public void testSendOneway() {
        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        Assert.assertEquals(TOPIC, message.header().getDestination());
        Assert.assertEquals("test_body", new String(message.getData()));

        producer.sendOneway(message);
    }

    @Test
    public void testBatchSend() {
        List<Message> messages = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TOPIC, "test_body".getBytes());
            Assert.assertEquals(TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));
            messages.add(message);
        }
        producer.send(messages);
    }

    @Test
    public void testBatchSendAsync() {
        List<Message> messages = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TOPIC, "test_body".getBytes());
            Assert.assertEquals(TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));
            messages.add(message);
        }
        SendResult sendResult = producer.sendAsync(messages).get();
        Assert.assertEquals(null, sendResult.messageId());
    }

    @Test
    public void testBatchSendOneway() {
        List<Message> messages = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TOPIC, "test_body".getBytes());
            Assert.assertEquals(TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));
            messages.add(message);
        }
        producer.sendOneway(messages);
    }

    @Test
    public void testInterceptor() {
        int[] preSend = {0};
        int[] postSend = {0};
        int send = 0;

        ProducerInterceptor producerInterceptor = new ProducerInterceptor() {
            @Override
            public void preSend(Message message, Context context) {
                context.attributes().put("property_2", "property_2_value");
                Assert.assertEquals("property_1_value", message.properties().getString("property_1"));
                preSend[0]++;
            }

            @Override
            public void postSend(Message message, Context context) {
                Assert.assertEquals("property_1_value", message.properties().getString("property_1"));
                Assert.assertEquals("property_2_value", context.attributes().getString("property_2"));
                postSend[0]++;
            }
        };

        producer.addInterceptor(producerInterceptor);

        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        message.properties().put("property_1", "property_1_value");
        producer.send(message);
        send++;

        Assert.assertEquals(send, preSend[0]);
        Assert.assertEquals(send, postSend[0]);

        producer.removeInterceptor(producerInterceptor);
        preSend[0] = 0;
        postSend[0] = 0;

        producer.send(message);
        send++;

        Assert.assertEquals(0, preSend[0]);
        Assert.assertEquals(0, postSend[0]);
    }

    @Test
    public void testPrepare1() {
        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        Assert.assertEquals(TOPIC, message.header().getDestination());
        Assert.assertEquals("test_body", new String(message.getData()));
        message.extensionHeader().get().setTransactionId("test_transactionId");
        Assert.assertEquals("test_transactionId", message.extensionHeader().get().getTransactionId());

        TransactionalResult transactionalResult = producer.prepare(message);
        Assert.assertEquals(null, transactionalResult.messageId());
        transactionalResult.commit();
    }

    @Test
    public void testPrepare2() {
        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        Assert.assertEquals(TOPIC, message.header().getDestination());
        Assert.assertEquals("test_body", new String(message.getData()));
        message.extensionHeader().get().setTransactionId("test_transactionId");
        Assert.assertEquals("test_transactionId", message.extensionHeader().get().getTransactionId());

        TransactionalResult transactionalResult = producer.prepare(message);
        Assert.assertEquals(null, transactionalResult.messageId());
        transactionalResult.rollback();
    }

    @Test
    public void testPrepare3() {
        ExtensionTransactionalResult transactionalResult = producer.prepare("test_transactionId");

        Message message = producer.createMessage(TOPIC, "test_body".getBytes());
        transactionalResult.send(message);
        transactionalResult.commit();
    }

    @Test
    public void testPrepare4() {
        ExtensionTransactionalResult transactionalResult = producer.prepare("test_transactionId");
        List<Message> messages = Lists.newLinkedList();
        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TOPIC, "test_body".getBytes());
            messages.add(message);
        }
        transactionalResult.send(messages);
        transactionalResult.commit();
    }

    @Test
    public void testTransactionCheck() throws Exception {
        int send = 0;
        int[] check = {0};

        ExtensionProducer producer = (ExtensionProducer) messagingAccessPoint.createProducer(new TransactionStateCheckListener() {
            @Override
            public void check(Message message, TransactionalContext context) {
                Assert.assertEquals(TRANSACTION_TOPIC, message.header().getDestination());
                check[0]++;
                context.commit();
            }
        });
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TRANSACTION_TOPIC, "test_body".getBytes());
            Assert.assertEquals(TRANSACTION_TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));

            message.extensionHeader().get().setTransactionId("test_transactionId_" + i);
            TransactionalResult transactionalResult = producer.prepare(message);

            if (i == 9) {
                transactionalResult.rollback();
            } else {
                send++;
            }
        }

        Thread.currentThread().sleep(1000 * 20);

        Assert.assertEquals(send, check[0]);
    }
}