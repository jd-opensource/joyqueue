package io.chubao.joyqueue.broker.protocol;

/**
 * JoyQueueContextAware
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface JoyQueueContextAware {

    void setJoyQueueContext(JoyQueueContext joyQueueContext);
}