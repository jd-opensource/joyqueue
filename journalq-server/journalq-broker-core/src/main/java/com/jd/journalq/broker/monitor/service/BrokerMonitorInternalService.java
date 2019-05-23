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
package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.broker.monitor.BrokerStartupInfo;
import com.jd.journalq.broker.monitor.stat.BrokerStatExt;
import com.jd.journalq.monitor.BrokerMonitorInfo;

/**
 * broker monitor service
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface BrokerMonitorInternalService {

    // TODO jvm监控

    /**
     * 获取broker信息
     *
     * @return
     */
    BrokerMonitorInfo getBrokerInfo();

    /**
     * thread safe
     * broker state 扩展信息,扩展信息包含topic>app>partitionGroup>partition积压
     * and broker id
     * @return  BrokerStatExt
     **/
    BrokerStatExt getExtendBrokerStat(long timeStamp);

    /**
     * 获取启动信息
     * @return
     */
    BrokerStartupInfo getStartInfo();
}