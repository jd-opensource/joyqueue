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
package com.jd.journalq.registry.zookeeper.manager;

import com.jd.journalq.registry.listener.ChildrenChangeListener;
import com.jd.journalq.registry.listener.ChildrenEvent;
import com.jd.journalq.registry.zookeeper.ZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 子节点变化管理器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/6/13 17:44
 */
public class ChildrenChangeManager extends ListenerManager<ChildrenChangeListener, ChildrenEvent> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public ChildrenChangeManager(ZKClient zkClient, String path) {
        super(zkClient, path);
    }

    @Override
    protected void onUpdateEvent() throws Exception {
        if (!super.isStarted()) {
            return;
        }

        super.writeLock.lock();
        try {
            // 注册watcher, 不需要数据
            super.zkClient.getChildren(super.path, super.updateWatcher);
            super.events.add(new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_UPDATED, super.path, null));
        } finally {
            super.writeLock.unlock();
        }
    }
}