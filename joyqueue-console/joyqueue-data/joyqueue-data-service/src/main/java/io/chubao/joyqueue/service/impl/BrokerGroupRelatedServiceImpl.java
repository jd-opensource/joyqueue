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
package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.BrokerGroupRelated;
import io.chubao.joyqueue.model.query.QBrokerGroupRelated;
import io.chubao.joyqueue.repository.BrokerGroupRelatedRepository;
import io.chubao.joyqueue.service.BrokerGroupRelatedService;
import org.springframework.stereotype.Service;

/**
 * 分组服务实现
 * Created by chenyanying3 on 2018-10-18
 */
@Service("brokerGroupRelatedService")
public class BrokerGroupRelatedServiceImpl extends PageServiceSupport<BrokerGroupRelated, QBrokerGroupRelated, BrokerGroupRelatedRepository> implements BrokerGroupRelatedService {

    @Override
    public int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated) {
        return repository.updateGroupByGroupId(brokerGroupRelated);
    }

    @Override
    public int deleteByGroupId(long groupId) {
        return repository.deleteByGroupId(groupId);
    }
}
