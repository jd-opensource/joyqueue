package com.jd.journalq.registry.listener;

import com.jd.journalq.toolkit.concurrent.EventListener;

/**
 * 连接事件监听器<br>
 * 由于单线程通知监听器，期望采用异步处理，加快速度<br>
 * 避免阻塞其它监听器获取事件和阻塞通知后续到达的连接事件
 */
public interface ConnectionListener extends EventListener<ConnectionEvent> {

    /**
     * 广播事件
     *
     * @param event
     */
    void onEvent(ConnectionEvent event);

}
