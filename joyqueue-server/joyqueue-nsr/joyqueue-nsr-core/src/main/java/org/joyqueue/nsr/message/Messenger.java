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
package org.joyqueue.nsr.message;


import com.jd.laf.extension.Type;
import org.joyqueue.domain.Broker;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * @param <E>
 */
public interface Messenger<E extends MetaEvent> extends LifeCycle, Type<String> {

    /**
     * 推送消息
     * @param event
     * @param brokers
     */
    void publish(E event, List<Broker> brokers);

    /**
     * 推送消息
     * @param event
     * @param brokers
     */
    void publish(E event, Broker... brokers);

    /**
     * 推送消息
     * @param event
     * @param brokers
     */
    void fastPublish(E event, List<Broker> brokers);

    /**
     * 推送消息
     * @param event
     * @param brokers
     */
    void fastPublish(E event, Broker... brokers);

    /**
     * 添加监听器
     * @param listener
     */
    void addListener(MessageListener listener);
}