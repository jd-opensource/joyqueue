package com.jd.journalq.registry.listener;

import com.jd.journalq.toolkit.URL;

/**
 * 连接事件
 */
public class ConnectionEvent {

    private ConnectionEventType type;
    private URL url;

    public ConnectionEvent(ConnectionEventType type, URL url) {
        this.type = type;
        this.url = url;
    }

    public ConnectionEventType getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ConnectionEvent [type=" + type + ", url=" + url + "]";
    }

    public enum ConnectionEventType {

        /**
         * 第一次连接或者session过期后重连
         */
        CONNECTED,

        /**
         * 闪断，session未过期
         */
        SUSPENDED,

        /**
         * session未过期，重连成功
         */
        RECONNECTED,

        /**
         * 没连上，并且session失效
         */
        LOST,

        /**
         * 超过最大重试次数(connectionRetryTimes)并且没有重新连接上
         * (如果connectionRetryTimes=0时不会产生此事件)
         */
        FAILED
    }

}
