package com.jd.journalq.nsr.model;

import com.jd.journalq.model.Query;

public class ConfigQuery implements Query {
    /**
     * 分组
     */
    private String group;
    /**
     * 键
     */
    private String key;
    /**
     * 关键字
     */
    private String keyword;

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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
