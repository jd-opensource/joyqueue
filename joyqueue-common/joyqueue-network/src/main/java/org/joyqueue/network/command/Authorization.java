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
package org.joyqueue.network.command;

import org.joyqueue.network.session.Language;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import com.google.common.base.Preconditions;

import com.google.common.base.Objects;

/**
 * 添加连接
 */
public class Authorization extends JoyQueuePayload {
    // 应用
    private String app;
    // 密码
    private String token;
    // 版本
    private String clientVersion;
    // 语言
    private Language language = Language.JAVA;

    public Authorization() {
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }


    public Authorization token(final String token) {
        setToken(token);
        return this;
    }

    public Authorization app(final String app) {
        setApp(app);
        return this;
    }

    public Authorization clientVersion(final String version) {
        setClientVersion(version);
        return this;
    }

    public Authorization language(final Language language) {
        setLanguage(language);
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }


    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(token != null && !token.isEmpty(), " token can not be null");
        Preconditions.checkArgument(app != null && !app.isEmpty(), " app can not be null");
        Preconditions.checkArgument(clientVersion != null && !clientVersion.isEmpty(), " clientVersion can not be null");
        Preconditions.checkArgument(language != null, " clientVersion can not be null");
    }

    @Override
    public int type() {
        return CommandType.AUTHORIZATION;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Authorization{");
        sb.append("app='").append(app).append('\'');
        sb.append(", token='").append(token).append('\'');
        sb.append(", clientVersion='").append(clientVersion).append('\'');
        sb.append(", language=").append(language);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authorization that = (Authorization) o;
        return Objects.equal(app, that.app) &&
                Objects.equal(token, that.token) &&
                Objects.equal(clientVersion, that.clientVersion) &&
                language == that.language;
    }

    @Override
    public int hashCode() {
        int result = app != null ? app.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (clientVersion != null ? clientVersion.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        return result;
    }

}