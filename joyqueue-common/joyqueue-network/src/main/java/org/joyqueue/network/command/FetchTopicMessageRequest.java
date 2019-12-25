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

import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * FetchTopicMessageRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchTopicMessageRequest extends JoyQueuePayload {

    private Map<String, FetchTopicMessageData> topics;
    private String app;
    private int ackTimeout;
    private int longPollTimeout;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_TOPIC_MESSAGE_REQUEST.getCode();
    }

    public void setTopics(Map<String, FetchTopicMessageData> topics) {
        this.topics = topics;
    }

    public Map<String, FetchTopicMessageData> getTopics() {
        return topics;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public int getLongPollTimeout() {
        return longPollTimeout;
    }

    public void setLongPollTimeout(int longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }
}
