package io.chubao.joyqueue.monitor;

/**
 * RestResponseCode
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/16
 */
public enum RestResponseCode {

    SUCCESS(200, "SUCCESS"),
    PARAM_ERROR(400, "PARAM_ERROR"),
    NOT_FOUND(404, "NOT_FOUND"),
    SERVER_ERROR(500, "SERVER_ERROR"),
    ;

    private int code;
    private String message;

    RestResponseCode(int code) {
        this.code = code;
    }

    RestResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}