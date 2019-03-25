package com.jd.journalq.common.exception;

/**
 * @author wylixiaobin
 * Date: 2018/9/19
 */
public class JMQConfigException extends RuntimeException{
    public JMQConfigException(String message) {
        super(message);
    }
    public JMQConfigException(String message,Throwable e) {
        super(message,e);
    }
    public JMQConfigException(String name, Object value, String message) {
        this("Invalid value " + value + " for configuration " + name + (message == null ? "" : ": " + message));
    }
}
