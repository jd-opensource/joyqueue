package io.chubao.joyqueue.exception;

/**
 * JoyQueueException
 * 14-4-19 上午10:41
 * @author Jame.HU
 * @version V1.0
 *
 */
public class JoyQueueException extends Exception {
    // 异常代码
    private int code;

    public JoyQueueException(int code) {
        this.code = code;
    }

    public JoyQueueException(String message, int code) {
        super(message);
        this.code = code;
    }

    public JoyQueueException(JoyQueueCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public JoyQueueException(JoyQueueCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public JoyQueueException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public JoyQueueException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
