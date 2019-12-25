/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AppToken)) return false;
        AppToken appToken = (AppToken) o;
        return Objects.equals(id, appToken.id) &&
                Objects.equals(app, appToken.app) &&
                Objects.equals(token, appToken.token) &&
                Objects.equals(effectiveTime.getTime() / 1000, appToken.effectiveTime.getTime()  / 1000) &&
                Objects.equals(expirationTime.getTime() / 1000, appToken.expirationTime.getTime() / 1000);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, app, token, effectiveTime, expirationTime);
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
