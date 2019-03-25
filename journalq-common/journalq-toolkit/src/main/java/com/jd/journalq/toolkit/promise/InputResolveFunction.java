package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface InputResolveFunction<T> extends InputOutputResolveFunction<T, Void> {
    void resolve0(T t) throws Exception;

    @Override
    default Void resolve(T t) throws Exception {
        resolve0(t);
        return null;
    }
}
