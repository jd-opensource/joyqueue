package io.openmessaging.jmq.consumer.support;

import com.jd.journalq.client.internal.consumer.BatchMessageListener;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.exception.IgnoreAckException;
import io.openmessaging.jmq.consumer.message.MessageConverter;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * BatchMessageListenerAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class BatchMessageListenerAdapter implements BatchMessageListener {

    private io.openmessaging.consumer.BatchMessageListener omsBatchMessageListener;

    public BatchMessageListenerAdapter(io.openmessaging.consumer.BatchMessageListener omsBatchMessageListener) {
        this.omsBatchMessageListener = omsBatchMessageListener;
    }

    @Override
    public void onMessage(List<ConsumeMessage> messages) {
        BatchMessageListenerContextAdapter context = new BatchMessageListenerContextAdapter();
        List<Message> omsMessages = MessageConverter.convertMessages(messages);
        omsBatchMessageListener.onReceived(omsMessages, context);

        if (!context.isAck()) {
            throw new IgnoreAckException();
        }
    }

    @Override
    public int hashCode() {
        return omsBatchMessageListener.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return omsBatchMessageListener.equals(obj);
    }

    @Override
    public String toString() {
        return omsBatchMessageListener.toString();
    }
}