package io.chubao.joyqueue.nsr.model;

import io.chubao.joyqueue.model.Query;

public class AppTokenQuery implements Query {
    /**
     * 应用
     */
    private String app;
    /**
     * token
     */
    private String token;

    public AppTokenQuery( ) {
    }

    public AppTokenQuery(String app, String token) {
        this.app = app;
        this.token = token;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

