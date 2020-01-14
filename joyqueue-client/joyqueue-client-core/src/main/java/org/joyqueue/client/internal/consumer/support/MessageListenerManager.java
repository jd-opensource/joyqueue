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
package org.joyqueue.client.internal.consumer.support;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.consumer.BaseMessageListener;
import org.joyqueue.client.internal.consumer.BatchMessageListener;
import org.joyqueue.client.internal.consumer.MessageListener;

import java.util.List;

/**
 * MessageListenerManager
 *
 * author: gaohaoxiang
 * date: 2018/12/25
 */
public class MessageListenerManager {

    private List<MessageListener> listeners = Lists.newLinkedList();
    private List<BatchMessageListener> batchListeners = Lists.newLinkedList();

    public void addListener(BaseMessageListener messageListener) {
        if (messageListener instanceof MessageListener) {
            listeners.add((MessageListener) messageListener);
        } else if (messageListener instanceof BatchMessageListener) {
            batchListeners.add((BatchMessageListener) messageListener);
        }
    }

    public void removeListener(BaseMessageListener messageListener) {
        listeners.remove(messageListener);
        batchListeners.remove(messageListener);
    }

    public boolean isEmpty() {
        return listeners.isEmpty() && batchListeners.isEmpty();
    }

    public List<MessageListener> getListeners() {
        return listeners;
    }

    public List<BatchMessageListener> getBatchListeners() {
        return batchListeners;
    }

}