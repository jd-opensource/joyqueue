package io.chubao.joyqueue.toolkit.ref;

/**
 * 引用计数
 */
public interface Reference {

    /**
     * 获取引用
     *
     */
    void acquire();

    /**
     * 释放引用.
     *
     * @return 是否都已经释放了.
     */
    boolean release();

    /**
     * 获取引用次数
     *
     * @return 引用次数.
     */
    long references();

}
