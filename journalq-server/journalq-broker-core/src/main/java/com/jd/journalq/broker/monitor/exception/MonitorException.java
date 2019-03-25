package com.jd.journalq.broker.monitor.exception;

/**
 * monitor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class MonitorException extends RuntimeException {

    public MonitorException() {
    }

    public MonitorException(String message) {
        super(message);
    }

    public MonitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitorException(Throwable cause) {
        super(cause);
    }

    public MonitorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}