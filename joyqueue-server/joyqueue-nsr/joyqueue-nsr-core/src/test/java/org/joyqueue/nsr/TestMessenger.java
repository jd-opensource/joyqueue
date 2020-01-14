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
package org.joyqueue.nsr;

import org.joyqueue.domain.Broker;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.message.MessageListener;
import org.joyqueue.nsr.message.Messenger;

import java.util.List;

/**
 * TestMessenger
 * author: gaohaoxiang
 * date: 2019/12/9
 */
public class TestMessenger implements Messenger {
    @Override
    public void publish(MetaEvent event, List list) {

    }

    @Override
    public void publish(MetaEvent event, Broker... brokers) {

    }

    @Override
    public void fastPublish(MetaEvent event, Broker... brokers) {

    }

    @Override
    public void addListener(MessageListener listener) {

    }

    @Override
    public void fastPublish(MetaEvent event, List list) {

    }

    @Override
    public Object type() {
        return "test";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }
}