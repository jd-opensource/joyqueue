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

import java.util.List;

public class TopicPubSub {

    private SlimTopic topic;
    private List<SlimApplication> producers;
    private List<SlimApplication> consumers;

    public SlimTopic getTopic() {
        return topic;
    }

    public void setTopic(SlimTopic topic) {
        this.topic = topic;
    }


    public List<SlimApplication> getProducers() {
        return producers;
    }

    public void setProducers(List<SlimApplication> producers) {
        this.producers = producers;
    }

    public List<SlimApplication> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<SlimApplication> consumers) {
        this.consumers = consumers;
    }
}
