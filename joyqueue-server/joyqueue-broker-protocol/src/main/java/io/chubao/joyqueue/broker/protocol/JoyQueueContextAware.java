package io.chubao.joyqueue.broker.protocol;

/**
 * JoyQueueContextAware
 *
 * author: gaohaoxiang
 * date: 2019/2/28
 */
public interface JoyQueueContextAware {

    void setJoyQueueContext(JoyQueueContext joyQueueContext);
}