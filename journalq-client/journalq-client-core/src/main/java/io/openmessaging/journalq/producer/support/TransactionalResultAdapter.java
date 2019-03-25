package io.openmessaging.journalq.producer.support;

import com.jd.journalq.client.internal.producer.MessageProducer;
import com.jd.journalq.client.internal.producer.TransactionMessageProducer;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import io.openmessaging.journalq.config.ExceptionConverter;
import io.openmessaging.journalq.producer.message.MessageAdapter;
import io.openmessaging.message.Message;
import io.openmessaging.producer.TransactionalResult;

/**
 * TransactionalResultAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class TransactionalResultAdapter implements TransactionalResult {

    private Message message;
    private String transactionId;
    private MessageProducer messageProducer;
    private TransactionMessageProducer transactionMessageProducer;

    public TransactionalResultAdapter(Message message, String transactionId, MessageProducer messageProducer) {
        this.message = message;
        this.transactionId = transactionId;
        this.messageProducer = messageProducer;
        this.transactionMessageProducer = prepare(message, transactionId, messageProducer);
    }

    protected TransactionMessageProducer prepare(Message message, String transactionId, MessageProducer messageProducer) {
        MessageAdapter messageAdapter = (MessageAdapter) message;
        ProduceMessage produceMessage = messageAdapter.getProduceMessage();
        TransactionMessageProducer transactionMessageProducer = messageProducer.beginTransaction(transactionId);
        transactionMessageProducer.send(produceMessage);
        return transactionMessageProducer;
    }

    @Override
    public String transactionId() {
        return transactionId;
    }

    @Override
    public void commit() {
        try {
            transactionMessageProducer.commit();
        } catch (Exception e) {
            throw ExceptionConverter.convertProduceException(e);
        }
    }

    @Override
    public void rollback() {
        try {
            transactionMessageProducer.rollback();
        } catch (Exception e) {
            throw ExceptionConverter.convertProduceException(e);
        }
    }

    @Override
    public String messageId() {
        return null;
    }
}