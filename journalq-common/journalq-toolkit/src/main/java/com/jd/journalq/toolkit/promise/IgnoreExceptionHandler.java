package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface IgnoreExceptionHandler extends ExceptionHandler{
    boolean handleIgnoreParameter(Exception e);
    @Override
    default boolean handle(Object ignored, Exception e) {
        return handleIgnoreParameter(e);
    }
}
