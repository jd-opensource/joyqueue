package com.jd.journalq.common.exception;

/**
 * JMQ异常
 *
 * @author Jame.HU
 * @version V1.0
 * @date 14-4-19 上午10:41
 */
public class JMQException extends Exception {
    // 异常代码
    private int code;

    public JMQException(int code) {
        this.code = code;
    }

    public JMQException(String message, int code) {
        super(message);
        this.code = code;
    }

    public JMQException(JMQCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public JMQException(JMQCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public JMQException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public JMQException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
