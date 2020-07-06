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
package org.joyqueue.repository;

import org.joyqueue.model.domain.BrokerGroup;
import org.joyqueue.model.query.QBrokerGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分组 仓库
 * Created by chenyanying3 on 2018-10-18
 */
@Repository
public interface BrokerGroupRepository extends PageRepository<BrokerGroup, QBrokerGroup> {
    /**
     * 查找全部
     * @return
     */
    List<BrokerGroup> findAll(QBrokerGroup qBrokerGroup);
}
