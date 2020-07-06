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
package org.joyqueue.model.domain;

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
