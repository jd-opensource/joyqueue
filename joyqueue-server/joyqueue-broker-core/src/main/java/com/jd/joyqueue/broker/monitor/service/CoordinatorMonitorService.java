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
package com.jd.joyqueue.broker.monitor.service;

import com.jd.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import com.jd.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata;
import com.jd.joyqueue.broker.coordinator.group.domain.GroupMetadata;

import java.util.Map;

/**
 * CoordinatorMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public interface CoordinatorMonitorService {

    /**
     * 查找协调者
     *
     * @param groupId
     * @return
     */
    CoordinatorDetail getCoordinator(String groupId);

    /**
     * 获得协调者组
     *
     * @param namespace
     * @param groupId
     * @param topic
     * @param isFormat
     * @return
     */
    GroupMetadata getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat);

    /**
     * 获得协调者组成员
     *
     * @param namespace
     * @param groupId
     * @param topic
     * @param isFormat
     * @return
     */
    Map<String, GroupMemberMetadata> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat);
}