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
package org.joyqueue.service;

import org.joyqueue.model.domain.BrokerGroupRelated;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.query.QBrokerGroupRelated;

import java.util.List;
import java.util.Map;

/**
 * 分组服务
 * Created by chenyanying3 on 2018-10-18.
 */
public interface BrokerGroupRelatedService extends PageService<BrokerGroupRelated, QBrokerGroupRelated> {
    int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated);
    int deleteByGroupId(long groupId);
    Map<Long, Identity> findGroupByBrokerIds(List<Long> brokerIds);
}
