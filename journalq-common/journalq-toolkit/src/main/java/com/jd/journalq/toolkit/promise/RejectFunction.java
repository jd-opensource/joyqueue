package com.jd.journalq.toolkit.promise;

/**
 * @author liyue25
 * Date: 2018/10/25
 */
public interface RejectFunction<P> {
     void handle(P p, Exception exception);
}
