package com.jd.journalq.network.transport.command;

import com.jd.journalq.network.transport.codec.JMQHeader;

/**
 * jmq消息体
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public abstract class JMQPayload implements Payload, Type, HeaderAware {

    private JMQHeader header;

    /**
     * 校验
     */
    public void validate() {
        //Do nothing
    }

    @Override
    public void setHeader(Header header) {
        if (header instanceof JMQHeader) {
            this.header = (JMQHeader) header;
        }
    }

    public JMQHeader getHeader() {
        return header;
    }
}