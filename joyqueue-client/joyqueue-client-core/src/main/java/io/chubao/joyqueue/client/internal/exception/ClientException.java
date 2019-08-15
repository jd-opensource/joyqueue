package io.chubao.joyqueue.client.internal.exception;

/**
 * ClientException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class ClientException extends RuntimeException {

    private int code;

    public ClientException() {
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }

    public ClientException(String error, int code) {
        super(error);
        this.code = code;
    }

    public ClientException(String error, int code, Throwable cause) {
        super(error, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}