package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface TerminateIgnoreExceptionHandler extends ExceptionHandler {
    void handleTerminalIgnore(Exception e);

    @Override
    default boolean handle(Object ignored, Exception e) {
        handleTerminalIgnore(e);
        return true;
    }
}
