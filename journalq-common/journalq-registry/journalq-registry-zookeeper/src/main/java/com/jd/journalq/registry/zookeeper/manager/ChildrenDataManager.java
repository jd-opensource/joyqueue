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

import com.jd.journalq.registry.PathData;
import com.jd.journalq.registry.listener.ChildrenEvent;
import com.jd.journalq.registry.util.Path;
import com.jd.journalq.registry.zookeeper.ZKClient;
import com.jd.journalq.registry.listener.ChildrenDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * 子节点变化及数据修改管理
 *
 * @author 何小锋，朱妙文
 */
public class ChildrenDataManager extends ListenerManager<ChildrenDataListener, ChildrenEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ChildrenDataManager.class);
    // 缓存子节点及其数据，键为叶子节点名称
    private Map<String, PathData> cache = new HashMap<String, PathData>();

    public ChildrenDataManager(ZKClient zkClient, String path) {
        super(zkClient, path);
    }

    @Override
    protected void doStop() {
        // 清空上次缓存的数据
        cache = new HashMap<String, PathData>();
        super.doStop();
    }

    @Override
    protected void onAddListener(final ChildrenDataListener listener) {
        if (!cache.isEmpty()) {
            for (PathData data : cache.values()) {
                String path = Path.concat(this.path, data.getPath());
                ChildrenEvent event = new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_CREATED, path, data.getData());
                events.add(event, listener);
            }
        }
    }

    @Override
    protected void onUpdateEvent() throws Exception {
        // 得到当前节点数据，注册监听器，当子节点数据发生变更的时候，要求修改该节点数据，减少监听器数量
        zkClient.getData(path, updateWatcher);
        // 得到子节点数据
        List<String> childs = zkClient.getChildren(path, updateWatcher);
        Set<String> children = new HashSet<String>(childs);
        List<String> added = new ArrayList<String>();
        List<String> removed = new ArrayList<String>();
        List<String> updated = new ArrayList<String>();
        Map<String, PathData> result = new HashMap<String, PathData>();
        PathData last;
        PathData current;

        writeLock.lock();
        try {
            if (!isStarted()) {
                // 防止终止了
                return;
            }
            // 遍历当前节点
            for (String child : children) {
                last = cache.get(child);
                String path = Path.concat(this.path, child);
                current = zkClient.getData(path, updateWatcher);
                if (last == null) {
                    //以前不存在，新增的
                    added.add(child);
                    events.add(new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_CREATED, path, current.getData()));
                } else if (!Arrays.equals(last.getData(), current.getData())) {
                    //数据变化了
                    updated.add(child);
                    events.add(new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_UPDATED, path, current.getData()));
                }
                result.put(child, current);
            }
            // 得到删除的节点
            for (Map.Entry<String, PathData> entry : cache.entrySet()) {
                if (!children.contains(entry.getKey())) {
                    //删除的
                    removed.add(entry.getKey());
                    String path = Path.concat(this.path, entry.getKey());
                    events
                            .add(new ChildrenEvent(ChildrenEvent.ChildrenEventType.CHILD_REMOVED, path, entry.getValue().getData()));
                }
            }

            cache = result;
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
            if (!updated.isEmpty()) {
                logger.debug("updated children:" + updated.toString());
            }
        }

    }

}