/**
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
package com.jd.journalq.network.command;

import com.jd.journalq.network.session.ClientId;
import com.jd.journalq.network.session.Language;
import com.jd.journalq.network.transport.command.JournalqPayload;

/**
 * AddConnection
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionRequest extends JournalqPayload {

    private String username;
    private String password;
    private String token;
    private String app;
    private String region;
    private String namespace;
    private Language language = Language.JAVA;
    private ClientId clientId;

    @Override
    public int type() {
        return JournalqCommandType.ADD_CONNECTION_REQUEST.getCode();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public ClientId getClientId() {
        return clientId;
    }

    public void setClientId(ClientId clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "AddConnection{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", app='" + app + '\'' +
                ", region='" + region + '\'' +
                ", namespace='" + namespace + '\'' +
                ", language=" + language +
                ", clientId=" + clientId +
                '}';
    }
}