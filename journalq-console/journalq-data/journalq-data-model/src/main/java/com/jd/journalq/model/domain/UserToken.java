package com.jd.journalq.model.domain;

import java.util.Date;

public class UserToken extends BaseModel {

    private String code;
    private String token;
    private Date expireTime;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }
}
