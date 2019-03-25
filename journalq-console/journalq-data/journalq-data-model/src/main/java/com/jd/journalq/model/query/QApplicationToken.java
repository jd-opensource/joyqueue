package com.jd.journalq.model.query;

import com.jd.journalq.model.QKeyword;
import com.jd.journalq.model.domain.Identity;

/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class QApplicationToken extends QKeyword {

    private Identity application;

    private String token;

    public QApplicationToken() {
    }

    public QApplicationToken(Identity application, String token) {
        this.application = application;
        this.token = token;
    }

    public Identity getApplication() {
        return application;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setApplication(Identity application) {
        this.application = application;
    }
}
