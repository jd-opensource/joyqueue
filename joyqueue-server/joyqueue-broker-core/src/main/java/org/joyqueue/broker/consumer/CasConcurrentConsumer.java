package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.toolkit.service.Service;

/**
 * @author LiYue
 * Date: 2020/4/9
 */
public class CasConcurrentConsumer extends Service implements ConcurrentConsumer {
    @Override
    public PullResult getMessage(Consumer consumer, int count, long ackTimeout, long accessTimes, int concurrent) throws JoyQueueException {
        return null;
    }

    @Override
    public boolean acknowledge(MessageLocation[] locations, Consumer consumer, boolean isSuccessAck) throws JoyQueueException {
        return false;
    }
}
