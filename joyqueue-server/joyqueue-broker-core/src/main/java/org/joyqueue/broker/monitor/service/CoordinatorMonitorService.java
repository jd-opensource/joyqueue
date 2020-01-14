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
package org.joyqueue.broker.monitor.service;

import org.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import org.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.coordinator.group.domain.GroupMetadata;

import java.util.Map;

/**
 * CoordinatorMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public interface CoordinatorMonitorService {

    /**
     * 获取组的协调者详情
     *
     * @param groupId 组
     * @return 协调者详情
     */
    CoordinatorDetail getCoordinator(String groupId);

    /**
     * 获得协调者组元数据
     *
     * @param namespace 作用域
     * @param groupId 组
     * @param topic 主题
     * @param isFormat 是否格式化元数据
     * @return 组元数据
     */
    GroupMetadata getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat);

    /**
     * 获得协调者组成员
     *
     * @param namespace 作用域
     * @param groupId 组
     * @param topic 主题
     * @param isFormat 是否格式化元数据
     * @return 所有组成员
     */
    Map<String, GroupMemberMetadata> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat);
}