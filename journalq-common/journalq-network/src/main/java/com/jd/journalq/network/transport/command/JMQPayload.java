package com.jd.journalq.network.transport.command;

/**
 * jmq消息体
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public abstract class JMQPayload implements Payload, Type {

    /**
     * 校验
     */
    public void validate() {
        //Do nothing
    }
}