package com.jd.journalq.handler.message;

/**
 * 消息队列类型
 * Created by chenyanying3 on 19-3-3.
 */
public enum MessageType {

    AUDIT_LOG("audit_log", "审计日志"),
    OPER_LOG("oper_log", "操作日志");

    private String value;
    private String description;

    MessageType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String value() {
        return value;
    }

    public String description() {
        return description;
    }
}
