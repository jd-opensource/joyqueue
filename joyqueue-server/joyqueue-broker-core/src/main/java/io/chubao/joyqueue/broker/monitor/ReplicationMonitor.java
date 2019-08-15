/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.monitor;

/**
 * ReplicationMonitor
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public interface ReplicationMonitor {

    /**
     * 复制消息
     * @param topic
     * @param partitionGroup
     * @param count
     * @param size
     * @param time
     */
    void onReplicateMessage(String topic, int partitionGroup, long count, long size, double time);

    /**
     * 写入复制消息
     * @param topic
     * @param partitionGroup
     * @param count
     * @param size
     * @param time
     */
    void onAppendReplicateMessage(String topic, int partitionGroup, long count, long size, double time);
}