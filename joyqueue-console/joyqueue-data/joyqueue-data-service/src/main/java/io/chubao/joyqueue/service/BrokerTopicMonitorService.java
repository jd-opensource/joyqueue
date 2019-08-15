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
package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.monitor.Client;
import io.chubao.joyqueue.model.domain.BrokerTopicMonitor;
import io.chubao.joyqueue.model.query.QMonitor;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public interface BrokerTopicMonitorService {
    PageResult<BrokerTopicMonitor> queryTopicsPartitionMointor(QPageQuery<QMonitor> qPageQuery);
    PageResult<Client> queryClientConnectionDetail(QPageQuery<QMonitor> qPageQuery);
    PageResult<BrokerTopicMonitor> queryTopicsMointor(QPageQuery<QMonitor> qPageQuery);
    BrokerMonitorInfo findBrokerMonitor(Long brokerId);
    BrokerStartupInfo getStartupInfo(Long brokerId) throws Exception;
}
