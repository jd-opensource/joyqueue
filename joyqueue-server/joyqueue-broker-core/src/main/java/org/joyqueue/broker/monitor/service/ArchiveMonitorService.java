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

import org.joyqueue.monitor.ArchiveMonitorInfo;

import java.util.Map;

/**
 * Created by chengzhiliang on 2018/12/18.
 */
public interface ArchiveMonitorService {

    /**
     * 获取消费归档数量
     *
     * @return
     */
    long getConsumeBacklogNum();

    /**
     * 获取发送归档数量
     *
     * @return
     */
    long getSendBackLogNum();

    /**
     * 按主题获取发送归档数量
     *
     * @return
     */
    Map<String, Long> getSendBackLogNumByTopic();

    /**
     * 获取归档监控
     *
     * @return
     */
    ArchiveMonitorInfo getArchiveMonitorInfo();

}
