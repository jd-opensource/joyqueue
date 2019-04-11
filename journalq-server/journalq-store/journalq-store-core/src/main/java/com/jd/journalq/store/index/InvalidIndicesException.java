package com.jd.journalq.store.index;

/**
 * @author liyue25
 * Date: 2018/8/29
 */
public class InvalidIndicesException extends RuntimeException {
    InvalidIndicesException(String message) {
        super(message);
    }
}
