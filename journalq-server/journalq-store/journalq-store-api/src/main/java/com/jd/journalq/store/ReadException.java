package com.jd.journalq.store;

/**
 * @author liyue25
 * Date: 2018/9/17
 */
public class ReadException extends RuntimeException {
    public ReadException(String message) {
        super(message);
    }
    public ReadException(){
        super();
    }
    public ReadException(String message, Throwable t) {
        super(message, t);
    }

    public ReadException(Throwable t) {
        super(t);
    }
}
