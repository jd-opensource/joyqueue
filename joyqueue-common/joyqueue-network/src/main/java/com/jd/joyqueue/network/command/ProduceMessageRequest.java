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
package com.jd.joyqueue.network.command;

import com.jd.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * ProduceMessageRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageRequest extends JoyQueuePayload {

    private String app;
    private Map<String, ProduceMessageData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_REQUEST.getCode();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setData(Map<String, ProduceMessageData> data) {
        this.data = data;
    }

    public Map<String, ProduceMessageData> getData() {
        return data;
    }
}