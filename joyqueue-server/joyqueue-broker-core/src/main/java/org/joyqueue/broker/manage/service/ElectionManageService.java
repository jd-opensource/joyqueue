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
package org.joyqueue.broker.manage.service;

public interface ElectionManageService {

    /**
     * 恢复选举元数据
     */
    void restoreElectionMetadata();

    /**
     * 返回当前选举元数据
     *
     * @return 元数据
     */
    String describe();

    /**
     * 返回主题下分区组的选举元数据
     *
     * @param topic 主题
     * @param partitionGroup 分区组组
     * @return 元数据
     */
    String describeTopic(String topic, int partitionGroup);

    /**
     * 更新主题下分区组的选举轮次
     * @param topic 主题
     * @param partitionGroup 分区组
     * @param term 轮次
     */
    void updateTerm(String topic, int partitionGroup, int term);
}
