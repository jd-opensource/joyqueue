package io.chubao.joyqueue.model.domain;

import java.util.Date;

/**
 * Created by yangyang115 on 18-9-6.
 */
public class ApplicationToken extends BaseModel implements DurationTime {

    private Identity application;

    private String token;

    private Date effectiveTime;

    private Date expirationTime;

    public Identity getApplication() {
        return application;
    }

    public void setApplication(Identity application) {
        this.application = application;
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
        return "ApplicationToken{" +
                "id=" + id +
                "application=" + application +
                ", token='" + token + '\'' +
                ", effectiveTime='" + effectiveTime + '\'' +
                ", expirationTime='" + expirationTime + '\'' +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                '}';
    }
}
