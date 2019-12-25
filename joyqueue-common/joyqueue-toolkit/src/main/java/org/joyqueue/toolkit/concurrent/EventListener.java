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
package org.joyqueue.toolkit.concurrent;

/**
 * 事件监听器
 *
 * @author hexiaofeng
 * @since 2013-12-09
 */
public interface EventListener<E> {

    /**
     * 事件处理，不要抛出异常
     *
     * @param event 事件
     */
    void onEvent(E event);

    /**
     * 心跳事件
     */
    interface Heartbeat {

        /**
         * 是否要触发心跳
         *
         * @param now 当前事件
         * @return true 如果要触发心跳
         */
        boolean trigger(long now);
    }
}
