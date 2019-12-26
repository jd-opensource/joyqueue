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
package org.joyqueue.client.internal.cluster;

import com.google.common.base.Objects;

import java.util.List;

public class TopicAndApp {

    private String topic;
    private List<String> topics;
    private String app;

    public TopicAndApp() {

    }

    public TopicAndApp(String topic, String app) {
        this.topic = topic;
        this.app = app;
    }

    public TopicAndApp(List<String> topics, String app) {
        this.topics = topics;
        this.app = app;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getApp() {
        return app;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(topic, topics, app);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TopicAndApp)) {
            return false;
        }
        TopicAndApp target = (TopicAndApp) obj;
        return (target.getApp().equals(app)
                && Objects.equal(target.getTopic(), topic)
                && Objects.equal(target.getTopics(), topics));
    }
}