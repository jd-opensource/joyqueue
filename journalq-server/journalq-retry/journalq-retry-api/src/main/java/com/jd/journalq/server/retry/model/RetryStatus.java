package com.jd.journalq.server.retry.model;

/**
 * Created by chengzhiliang on 2019/2/20.
 */
public enum RetryStatus {

    RETRY_ING((short)1,"重试中"),
    RETRY_EXPIRE((short)-2,"过期"),
    RETRY_DELETE((short)-1,"删除"),
    RETRY_SUCCESS((short)0,"成功");

    private short value;
    private String name;

    RetryStatus(short value, String name) {
        this.value = value;
        this.name = name;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
