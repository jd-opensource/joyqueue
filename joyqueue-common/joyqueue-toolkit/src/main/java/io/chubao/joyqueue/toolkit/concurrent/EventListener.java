package io.chubao.joyqueue.toolkit.concurrent;

/**
 * 事件监听器
 *
 * @author hexiaofeng
 * @since 2013-12-09
 */
public interface EventListener<E> {

    /**
     * 事件处理，不要抛出异常
     *
     * @param event 事件
     */
    void onEvent(E event);

    /**
     * 心跳事件
     */
    interface Heartbeat {

        /**
         * 是否要触发心跳
         *
         * @param now 当前事件
         * @return true 如果要触发心跳
         */
        boolean trigger(long now);
    }
}
