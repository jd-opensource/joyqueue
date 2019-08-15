package io.openmessaging.joyqueue.producer.support;

import io.chubao.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.TransactionStatus;
import io.chubao.joyqueue.domain.TopicName;
import io.openmessaging.joyqueue.producer.message.MessageAdapter;
import io.openmessaging.message.Message;
import io.openmessaging.producer.TransactionStateCheckListener;

/**
 * TransactionStateCheckListenerAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransactionStateCheckListenerAdapter implements TxFeedbackCallback {

    private TransactionStateCheckListener transactionStateCheckListener;

    public TransactionStateCheckListenerAdapter(TransactionStateCheckListener transactionStateCheckListener) {
        this.transactionStateCheckListener = transactionStateCheckListener;
    }

    @Override
    public TransactionStatus confirm(TopicName topic, String txId, String transactionId) {
        Message message = new MessageAdapter(new ProduceMessage());
        message.header()
                .setDestination(topic.getFullName());
        message.extensionHeader().get()
                .setTransactionId(transactionId);

        TransactionalContextAdapter transactionalContext = new TransactionalContextAdapter();
        transactionStateCheckListener.check(message, transactionalContext);

        if (transactionalContext.getStatus() == null) {
            return TransactionStatus.UNKNOWN;
        }
        return transactionalContext.getStatus();
    }
}