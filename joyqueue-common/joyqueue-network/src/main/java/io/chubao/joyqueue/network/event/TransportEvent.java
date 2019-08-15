package io.chubao.joyqueue.network.event;

import io.chubao.joyqueue.network.transport.Transport;

/**
 * TransportEvent
 *
 * author: gaohaoxiang
 * date: 2018/8/15
 */
public class TransportEvent {

    // 类型
    private TransportEventType type;
    // 通道
    private Transport transport;

    public TransportEvent(TransportEventType type, Transport transport) {
        this.type = type;
        this.transport = transport;
    }

    public TransportEventType getType() {
        return this.type;
    }

    public Transport getTransport() {
        return this.transport;
    }
}