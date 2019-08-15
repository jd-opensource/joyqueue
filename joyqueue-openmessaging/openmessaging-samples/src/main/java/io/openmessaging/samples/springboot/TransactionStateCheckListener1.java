package io.openmessaging.samples.springboot;

import io.openmessaging.message.Message;
import io.openmessaging.producer.TransactionStateCheckListener;
import io.openmessaging.spring.boot.annotation.OMSTransactionStateCheckListener;

@OMSTransactionStateCheckListener
public class TransactionStateCheckListener1 implements TransactionStateCheckListener {

    @Override
    public void check(Message message, TransactionalContext context) {
        System.out.println(String.format("check, message: %s", message));
        context.commit();
    }
}