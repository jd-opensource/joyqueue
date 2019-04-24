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
package com.jd.journalq.registry.listener;

import com.jd.journalq.toolkit.concurrent.EventListener;

/**
 * 连接事件监听器<br>
 * 由于单线程通知监听器，期望采用异步处理，加快速度<br>
 * 避免阻塞其它监听器获取事件和阻塞通知后续到达的连接事件
 */
public interface ConnectionListener extends EventListener<ConnectionEvent> {

    /**
     * 广播事件
     *
     * @param event
     */
    void onEvent(ConnectionEvent event);

}
