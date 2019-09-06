package io.chubao.joyqueue.nsr.journalkeeper.domain;

import io.chubao.joyqueue.nsr.journalkeeper.helper.Column;

import java.util.Date;

/**
 * AppTokenDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class AppTokenDTO extends BaseDTO {

    private Long id;
    private String app;
    private String token;
    @Column(alias = "effective_time")
    private Date effectiveTime;
    @Column(alias = "expiration_time")
    private Date expirationTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Date effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
}