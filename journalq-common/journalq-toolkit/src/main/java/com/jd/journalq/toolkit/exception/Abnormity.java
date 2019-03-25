package com.jd.journalq.toolkit.exception;

/**
 * 出现异常接口
 * Created by hexiaofeng on 16-5-6.
 */
public interface Abnormity {
    /**
     * 出现异常
     *
     * @param e 异常
     * @return 异常后的处理
     */
    boolean onException(Throwable e);
}
