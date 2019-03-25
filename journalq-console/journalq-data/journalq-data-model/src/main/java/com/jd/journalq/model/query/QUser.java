package com.jd.journalq.model.query;

import com.jd.journalq.common.model.QKeyword;

/**
 * Created by yangyang36 on 2018/9/12.
 */
public class QUser extends QKeyword {

    private Integer status;

    private Long appId;

    public QUser() {

    }

    public QUser(String keyword, Integer status) {
        super(keyword);
        this.status = status;
    }

    public QUser(String keyword, Integer status, Long appId) {
        this(keyword, status);
        this.appId = appId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QUser{");
        sb.append("status=").append(status);
        sb.append("appId=").append(appId);
        sb.append(", keyword='").append(keyword).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
