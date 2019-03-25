package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/25
 */
public interface InputOutputResolveFunction<T,R>{

      R resolve(T t) throws Exception;

}
