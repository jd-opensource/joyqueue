package io.chubao.joyqueue.nsr.message;


/**
 * @param <E>
 */
public interface Messenger<E> {
    /**
     * 广播消息
     *
     * @param e
     */
    void publish(E e);

    /**
     * 添加监听
     *
     * @param listener
     */
    void addListener(MessageListener listener);
}
