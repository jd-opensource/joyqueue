package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;

/**
 * 心跳命令，只用于保持网络连接
 */
public class Heartbeat extends Joyqueue0Payload {

    @Override
    public int type() {
        return Joyqueue0CommandType.HEARTBEAT.getCode();
    }
}