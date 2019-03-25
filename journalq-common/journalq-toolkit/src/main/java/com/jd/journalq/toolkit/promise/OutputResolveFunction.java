package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface OutputResolveFunction<R> extends InputOutputResolveFunction<Void, R> {
    R resolve0() throws Exception;

    @Override
    default R resolve(Void nil) throws Exception {
        return resolve0();
    }
}
