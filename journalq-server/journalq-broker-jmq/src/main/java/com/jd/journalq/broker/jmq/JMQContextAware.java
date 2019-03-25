package com.jd.journalq.broker.jmq;

/**
 * JMQContextAware
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface JMQContextAware {

    void setJmqContext(JMQContext jmqContext);
}