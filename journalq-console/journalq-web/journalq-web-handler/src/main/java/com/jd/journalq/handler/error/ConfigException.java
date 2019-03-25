package com.jd.journalq.handler.error;

/**
 * 配置异常
 * Created by yangyang115 on 18-7-26.
 */
public class ConfigException extends RuntimeException {

    private int code;
    private int status;

    public ConfigException(ErrorCode code) {
        super(code.getMessage());
        this.code = code.getCode();
        this.status = code.getStatus();
    }

    public ConfigException(ErrorCode code, String message) {
        super(code.getMessage() + " " + message);
        this.code = code.getCode();
        this.status = code.getStatus();
    }

    public ConfigException(ErrorCode code, Throwable throwable) {
        super(throwable);
        this.code = code.getCode();
        this.status = code.getStatus();
    }

    public int getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
