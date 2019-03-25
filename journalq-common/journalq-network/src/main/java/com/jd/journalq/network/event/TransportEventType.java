package com.jd.journalq.network.event;

/**
 * 通信事件类型
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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