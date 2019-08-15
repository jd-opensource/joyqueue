package io.chubao.joyqueue.network.event;

/**
 * TransportEventType
 *
 * author: gaohaoxiang
 * date: 2018/8/15
 */
public enum TransportEventType {

    /**
     * 连接
     */
    CONNECT,

    /**
     * 重连
     */
    RECONNECT,

    /**
     * 关闭
     */
    CLOSE,

    /**
     * 异常
     */
    EXCEPTION
}