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
package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.broker.monitor.stat.BrokerStatExt;
import io.chubao.joyqueue.broker.monitor.stat.JVMStat;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.toolkit.vm.GCEventListener;

/**
 * broker monitor service
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public interface BrokerMonitorInternalService {

    /**
     * 获取监控信息
     *
     * @return broker信息
     */
    BrokerMonitorInfo getBrokerInfo();

    /**
     * 获取扩展监控信息，包括额外的积压信息等
     *
     * @param timeStamp 时间戳，会写回到返回值
     * @return broker扩展信息
     */
    BrokerStatExt getExtendBrokerStat(long timeStamp);

    /**
     * 获取启动信息
     *
     * @return
     */
    BrokerStartupInfo getStartInfo();

    /***
     *
     * GC event listener
     *
     **/
    void addGcEventListener(GCEventListener listener);


    /**
     *  当前获取JVM 信息, realtime
     *
     **/
    JVMStat getJVMState();
}