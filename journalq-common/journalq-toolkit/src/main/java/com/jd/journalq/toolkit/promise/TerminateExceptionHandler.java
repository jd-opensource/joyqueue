package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface TerminateExceptionHandler extends ExceptionHandler {
    void handleTerminal(Object p, Exception e);
    @Override
    default boolean handle(Object p, Exception e) {
        handleTerminal(p, e);
        return true;
    }
}
