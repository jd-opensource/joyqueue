package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public interface ResolveFunction extends InputOutputResolveFunction<Void, Void> {

    void resolve0() throws Exception;
    @Override
    default Void resolve(Void aVoid) throws Exception{
        resolve0();
        return null;
    }
}
