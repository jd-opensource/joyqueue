package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;

/**
 * 心跳命令，只用于保持网络连接
 */
public class Heartbeat extends JMQ2Payload {

    @Override
    public int type() {
        return JMQ2CommandType.HEARTBEAT.getCode();
    }
}