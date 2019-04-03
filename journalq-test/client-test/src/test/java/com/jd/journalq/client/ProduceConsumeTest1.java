package com.jd.journalq.client;

import io.openmessaging.KeyValue;
import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.journalq.domain.JournalQConsumerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQProducerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQTxFeedbackBuiltinKeys;
import io.openmessaging.journalq.producer.ExtensionProducer;
import io.openmessaging.journalq.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;
import io.openmessaging.producer.TransactionStateCheckListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/13
 */
public class ProduceConsumeTest1 extends AbstractClientTest {

    public static final String TOPIC = "test_produceconsume_1";

    public static final String BROADCAST_TOPIC = "test_produceconsume_broadcast_1";

    public Producer producer;
    public Consumer consumer;

    @Before
    public void before() {
        super.before();

        producer = messagingAccessPoint.createProducer();
        producer.start();

        consumer = messagingAccessPoint.createConsumer();
        consumer.start();
    }

    @Override
    protected KeyValue getAttributes() {
        KeyValue keyValue = super.getAttributes();
        keyValue.put(JournalQConsumerBuiltinKeys.LONGPOLL_TIMEOUT, -1);
        keyValue.put(JournalQConsumerBuiltinKeys.BROADCAST_LOCAL_PATH, "/export/Data/jmq/broadcast");
        keyValue.put(JournalQProducerBuiltinKeys.TRANSACTION_TIMEOUT, 1000 * 10);
        keyValue.put(JournalQTxFeedbackBuiltinKeys.FETCH_INTERVAL, 1);
        keyValue.put(JournalQTxFeedbackBuiltinKeys.FETCH_SIZE, 10);
        return keyValue;
    }

    @Test
    public void testProduceConsume() throws Exception {
        int send = 0;
        int[] received = {0};

        consumer.bindQueue(TOPIC, new BatchMessageListener() {
            @Override
            public void onReceived(List<Message> list, Context context) {
                for (Message message : list) {
                    Assert.assertEquals(TOPIC, message.header().getDestination());
                }
                received[0] += list.size();
            }
        });

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TOPIC, "test_body".getBytes());
            Assert.assertEquals(TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));

            SendResult sendResult = producer.send(message);
            Assert.assertEquals("-1", sendResult.messageId());
            send++;
        }

        Thread.currentThread().sleep(1000 * 5);

        Assert.assertEquals(send, received[0]);
    }

    @Test
    public void testBroadcastProduceConsume() throws Exception {
        int send = 0;
        int[] received = {0};

        consumer.bindQueue(BROADCAST_TOPIC, new BatchMessageListener() {
            @Override
            public void onReceived(List<Message> list, Context context) {
                for (Message message : list) {
                    Assert.assertEquals(BROADCAST_TOPIC, message.header().getDestination());
                }
                received[0] += list.size();
            }
        });

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(BROADCAST_TOPIC, "test_body".getBytes());
            Assert.assertEquals(BROADCAST_TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));

            SendResult sendResult = producer.send(message);
            Assert.assertEquals("-1", sendResult.messageId());
            send++;
        }

        Thread.currentThread().sleep(1000 * 5);

        Assert.assertEquals(send, received[0]);
    }

    @Test
    public void testTransactionCheckAndConsume() throws Exception {
        int send = 0;
        int[] check = {0};
        int[] consume = {0};
        boolean[] isChecked = {false};

        consumer.bindQueue(TOPIC, new BatchMessageListener() {
            @Override
            public void onReceived(List<Message> list, Context context) {
                for (Message message : list) {
                    Assert.assertEquals(TOPIC, message.header().getDestination());
                }
                if (isChecked[0]) {
                    consume[0] += list.size();
                }
            }
        });

        ExtensionProducer producer = (ExtensionProducer) messagingAccessPoint.createProducer(new TransactionStateCheckListener() {
            @Override
            public void check(Message message, TransactionalContext context) {
                Assert.assertEquals(TOPIC, message.header().getDestination());
                check[0]++;
                isChecked[0] = true;
                context.commit();
            }
        });
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage(TOPIC, "test_body".getBytes());
            Assert.assertEquals(TOPIC, message.header().getDestination());
            Assert.assertEquals("test_body", new String(message.getData()));

            ExtensionTransactionalResult transactionalResult = producer.prepare("test_transactionId_" + i);
            transactionalResult.send(message);
            send++;
        }

        Thread.currentThread().sleep(1000 * 20);

        Assert.assertEquals(send, check[0]);
        Assert.assertEquals(check[0], consume[0]);
    }
}