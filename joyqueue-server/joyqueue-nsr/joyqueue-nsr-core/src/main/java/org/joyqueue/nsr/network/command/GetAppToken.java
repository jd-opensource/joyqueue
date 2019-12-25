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
package org.joyqueue.nsr.network.command;

import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppToken extends JoyQueuePayload {
    private String app;
    private String token;
    public GetAppToken app(String app){
        this.app = app;
        return this;
    }
    public GetAppToken token(String token){
        this.token = token;
        return this;
    }

    public String getApp() {
        return app;
    }

    public String getToken() {
        return token;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN;
    }

    @Override
    public String toString() {
        return "GetAppToken{" +
                "app='" + app + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
