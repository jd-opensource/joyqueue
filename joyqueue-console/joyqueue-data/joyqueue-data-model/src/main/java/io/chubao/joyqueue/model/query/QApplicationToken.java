package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.QKeyword;
import io.chubao.joyqueue.model.domain.Identity;

/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class QApplicationToken extends QKeyword {

    private Identity application;

    private String token;

    public QApplicationToken() {
    }

    public QApplicationToken(String appCode) {
        application = new Identity(appCode);
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
