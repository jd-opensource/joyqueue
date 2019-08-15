package io.chubao.joyqueue.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wylixiaobin
 * Date: 2018/11/26
 */
public class AppToken implements Serializable {

    protected Long id;

    protected String app;

    protected String token;

    protected Date effectiveTime;

    protected Date expirationTime;

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

    @Override
    public String toString() {
        return "AppToken{" +
                "id=" + id +
                ", app='" + app + '\'' +
                ", token='" + token + '\'' +
                ", effectiveTime=" + effectiveTime +
                ", expirationTime=" + expirationTime +
                '}';
    }
}
