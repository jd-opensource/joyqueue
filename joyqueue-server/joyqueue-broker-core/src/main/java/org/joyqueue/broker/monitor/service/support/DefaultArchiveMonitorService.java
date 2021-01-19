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
package org.joyqueue.broker.monitor.service.support;

import org.joyqueue.broker.archive.ArchiveManager;
import org.joyqueue.broker.monitor.service.ArchiveMonitorService;
import org.joyqueue.monitor.ArchiveMonitorInfo;

import java.util.Map;

/**
 * Created by chengzhiliang on 2018/12/18.
 */
public class DefaultArchiveMonitorService implements ArchiveMonitorService {

    private ArchiveManager archiveManager;

    public DefaultArchiveMonitorService(ArchiveManager archiveManager) {
        this.archiveManager = archiveManager;
    }

    @Override
    public long getConsumeBacklogNum() {
        return archiveManager.getConsumeBacklogNum();
    }

    @Override
    public long getSendBackLogNum() {
        return archiveManager.getSendBacklogNum();
    }

    @Override
    public Map<String, Long> getSendBackLogNumByTopic() {
        return archiveManager.getSendBacklogNumByTopic();
    }

    @Override
    public ArchiveMonitorInfo getArchiveMonitorInfo() {
        long consumeBacklogNum = getConsumeBacklogNum();
        long sendBackLogNum = getSendBackLogNum();
        ArchiveMonitorInfo info = new ArchiveMonitorInfo();
        info.setConsumeBacklog(consumeBacklogNum);
        info.setProduceBacklog(sendBackLogNum);
        info.setTopicProduceBacklog(archiveManager.getSendBacklogNumByTopic());

        return info;
    }
}
