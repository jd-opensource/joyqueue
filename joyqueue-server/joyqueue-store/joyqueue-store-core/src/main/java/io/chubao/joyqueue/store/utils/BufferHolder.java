package io.chubao.joyqueue.store.utils;

/**
 * 可管理的Buffer持有者
 *
 * @author liyue25
 * Date: 2019-03-28
 */
public interface BufferHolder extends Timed {
    /**
     * Buffer大小
     */
    int size();

    /**
     * 是否可以释放？
     */
    boolean isFree();

    /**
     * 尝试释放
     *
     * @return 释放成功返回true，否则返回false
     */
    boolean evict();
}
