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
package com.jd.joyqueue.registry.zookeeper.manager;

import com.jd.joyqueue.registry.listener.ChildrenEvent;
import com.jd.joyqueue.registry.listener.ChildrenListener;
import com.jd.joyqueue.registry.listener.LiveListener;
import com.jd.joyqueue.registry.util.Path;
import com.jd.joyqueue.registry.zookeeper.ZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 子节点变化监听器管理
 *
 * @author 何小锋
 */
public class ChildrenManager extends ListenerManager<ChildrenListener, ChildrenEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ChildrenManager.class);
    private Set<String> cache = new HashSet<String>();

    public ChildrenManager(ZKClient zkClient, String path) {
        super(zkClient, path);
    }

    @Override
    protected void doStop() {
        // 清理缓存
        cache = new HashSet<String>();
        super.doStop();
    }

    @Override
    protected void onAddListener(final ChildrenListener listener) {
        if (!cache.isEmpty() && !(listener instanceof LiveListener)) {
            for (String child : cache) {
                String path = Path.concat(this.path, child);
                ChildrenEvent event = new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_CREATED, path, null);
                events.add(event, listener);
            }
        }
    }

    @Override
    protected void onUpdateEvent() throws Exception {
        // 得到当前节点,只包含子节点的名称，不包括全路径
        List<String> childrenList = zkClient.getChildren(path, updateWatcher);
        Set<String> children = new HashSet<String>(childrenList);
        List<String> added = new ArrayList<String>();
        List<String> removed = new ArrayList<String>();
        writeLock.lock();
        try {
            if (!isStarted()) {
                // 已经关闭了
                return;
            }
            // 得到新增的节点
            for (String child : children) {
                if (!cache.contains(child)) {
                    //新增的
                    added.add(child);
                    events.add(new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_CREATED, Path.concat(path, child), null));
                }
            }
            // 得到删除的节点
            for (String child : cache) {
                if (!children.contains(child)) {
                    //删除的
                    removed.add(child);
                    events.add(new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_REMOVED, Path.concat(path, child), null));
                }
            }
            cache = children;
        } finally {
            writeLock.unlock();
        }
        if (logger.isDebugEnabled()) {
            if (!removed.isEmpty()) {
                logger.debug("removed children:" + removed.toString());
            }
            if (!added.isEmpty()) {
                logger.debug("added children:" + added.toString());
            }
        }
    }

}