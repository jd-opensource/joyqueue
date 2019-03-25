package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface CallbackFunction extends InputCallbackFunction<Void> {
    void callbackNoInput();
    @Override
    default void callback(Void nil) {
        callbackNoInput();
    }
}
