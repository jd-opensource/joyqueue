package com.jd.journalq.model;

/**
 * 关键字查询
 */
public class QKeyword extends QOperator {
    private long id;
    protected String keyword;

    public QKeyword() {
    }

    public QKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
