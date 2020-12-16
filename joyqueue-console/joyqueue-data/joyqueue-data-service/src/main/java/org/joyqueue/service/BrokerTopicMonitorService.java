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

import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.monitor.BrokerMonitorInfo;
import org.joyqueue.monitor.BrokerStartupInfo;
import org.joyqueue.monitor.Client;
import org.joyqueue.model.domain.BrokerTopicMonitor;
import org.joyqueue.model.query.QMonitor;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public interface BrokerTopicMonitorService {
    PageResult<BrokerTopicMonitor> queryTopicsPartitionMointor(QPageQuery<QMonitor> qPageQuery);
    PageResult<Client> queryClientConnectionDetail(QPageQuery<QMonitor> qPageQuery);
    PageResult<BrokerTopicMonitor> queryTopicsMointor(QPageQuery<QMonitor> qPageQuery);
    BrokerMonitorInfo findBrokerMonitor(Long brokerId);
    BrokerStartupInfo getStartupInfo(Long brokerId) throws Exception;
    List<String> queryTopicList(Long brokerId) throws Exception;
    List<BrokerTopicMonitor> queryTopicsPartitionMonitors(Integer brokerId);
}
