package com.jd.journalq.model.query;

import com.jd.journalq.common.model.QKeyword;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class QConfig extends QKeyword {
    private Integer status;
    private String group;
    private String key;

    public QConfig() {
    }

    public QConfig(String group) {
        this.group = group;
    }

    public QConfig(String group, String key) {
        this.group = group;
        this.key = key;
    }

    public QConfig(Integer status, String group, String key) {
        this.status = status;
        this.group = group;
        this.key = key;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
