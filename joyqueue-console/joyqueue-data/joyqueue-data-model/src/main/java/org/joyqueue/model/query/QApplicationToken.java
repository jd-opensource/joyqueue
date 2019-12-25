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
