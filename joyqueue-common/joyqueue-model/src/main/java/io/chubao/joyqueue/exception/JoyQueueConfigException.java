package io.chubao.joyqueue.exception;

/**
 * @author wylixiaobin
 * Date: 2018/9/19
 */
public class JoyQueueConfigException extends RuntimeException{
    public JoyQueueConfigException(String message) {
        super(message);
    }
    public JoyQueueConfigException(String message, Throwable e) {
        super(message,e);
    }
    public JoyQueueConfigException(String name, Object value, String message) {
        this("Invalid value " + value + " for configuration " + name + (message == null ? "" : ": " + message));
    }
}
