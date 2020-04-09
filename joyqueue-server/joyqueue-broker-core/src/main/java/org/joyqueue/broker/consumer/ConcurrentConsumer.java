package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.toolkit.lang.LifeCycle;

/**
 * @author LiYue
 * Date: 2020/4/9
 */
public interface ConcurrentConsumer extends LifeCycle {
    PullResult getMessage(Consumer consumer, int count, long ackTimeout, long accessTimes, int concurrent) throws JoyQueueException;

    boolean acknowledge(MessageLocation[] locations, Consumer consumer, boolean isSuccessAck) throws JoyQueueException;
}
