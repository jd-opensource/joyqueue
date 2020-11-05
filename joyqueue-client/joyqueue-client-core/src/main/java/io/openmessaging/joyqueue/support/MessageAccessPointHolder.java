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
package io.openmessaging.joyqueue.support;

import org.joyqueue.client.internal.MessageAccessPoint;

/**
 * MessageAccessPointHolder
 *  
 * author: gaohaoxiang
 * date: 2019/5/14
 */
public class MessageAccessPointHolder {

    private MessageAccessPoint messageAccessPoint;
    private int producers = 0;
    private int consumers = 0;

    public MessageAccessPointHolder(MessageAccessPoint messageAccessPoint) {
        this.messageAccessPoint = messageAccessPoint;
    }

    public MessageAccessPoint getMessageAccessPoint() {
        return messageAccessPoint;
    }

    public void stopProducer() {
        producers--;
        maybeStop();
    }

    /** start producer */
    public void startProducer() {
        producers++;
    }

    public void stopConsumer() {
        consumers--;
        maybeStop();
    }

    public void startConsumer() {
        consumers++;
    }

    protected void maybeStop() {
        if (producers != 0 || consumers != 0) {
            return;
        }
        messageAccessPoint.stop();
    }
}
