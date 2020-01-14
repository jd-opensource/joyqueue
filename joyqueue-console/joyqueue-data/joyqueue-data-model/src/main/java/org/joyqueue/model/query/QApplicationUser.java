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
package org.joyqueue.model.query;

import org.joyqueue.model.QKeyword;
import org.joyqueue.model.domain.Identity;

/**
 * 应用-用户关联关系
 * Created by chenyanying on 2018-10-17.
 */
public class QApplicationUser extends QKeyword {
    private Identity user;
    private Identity app;
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Identity getUser() {
        return user;
    }

    public void setUser(Identity user) {
        this.user = user;
    }

    public Identity getApp() {
        return app;
    }

    public void setApp(Identity app) {
        this.app = app;
    }

    public QApplicationUser(Identity user, Identity app, Integer status) {
        this.user = user;
        this.app = app;
        this.status = status;
    }

    public QApplicationUser(String keyword, Identity user, Identity app, Integer status) {
        super(keyword);
        this.user = user;
        this.app = app;
        this.status = status;
    }

    @Override
    public String toString() {
        return "QApplicationUser{" +
                "userId=" + user.getId() +
                ", userCode=" + user.getCode() +
                ", appId=" + app.getId() +
                ", appCode=" + app.getCode() +
                ", status=" + status +
                '}';
    }
}
