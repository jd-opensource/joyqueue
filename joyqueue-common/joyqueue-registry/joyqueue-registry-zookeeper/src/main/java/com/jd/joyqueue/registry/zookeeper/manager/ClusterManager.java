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

import com.jd.joyqueue.registry.PathData;
import com.jd.joyqueue.registry.listener.ClusterEvent;
import com.jd.joyqueue.registry.listener.ClusterListener;
import com.jd.joyqueue.registry.util.Path;
import com.jd.joyqueue.registry.zookeeper.ZKClient;
import com.google.common.base.Charsets;
import com.jd.joyqueue.registry.listener.Observer;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


/**
 * 集群管理器，Observer节点不参与选举，只接收选举结果
 */
public class ClusterManager extends ElectionManager<ClusterListener, ClusterEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
    // 以前的数据节点
    private Map<String, PathData> cache = new HashMap<String, PathData>();

    public ClusterManager(ZKClient zkClient, String path, String identity) {
        super(zkClient, path, identity);
    }

    @Override
    protected void onAddListener(final ClusterListener listener) {
        try {
            // 必须在锁里面，避免在更新数据，重复发送
            if (listener instanceof Observer) {
                observer = true;
            }
            if (!observer && node.get() == null) {
                // 初始化创建Leader选举的临时节点
                updateEvents.add(UpdateType.UPDATE);
            } else {
                // 广播事件给该监听器进行初始化
                ClusterEvent event = new ClusterEvent(path, zkClient.getSortedChildData(path));
                events.add(event, listener);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void createElectionNode() throws Exception {
        //增加IP地址，便于在Zookeeper中查看是那台机器创建的节点
        byte[] data = identity.getBytes(Charsets.UTF_8);

        // 先查找是否有原来遗留下来的选举节点
        try {
            List<PathData> children = zkClient.getChildData(path);
            for (PathData child : children) {
                if (Arrays.equals(data, child.getData())) {
                    zkClient.delete(Path.concat(path, child.getPath()));
                    break;
                }
            }
        } catch (Exception ignored) {
            // 可以忽略异常
        }

        //创建有序临时节点
        String childFullPath = zkClient.create(Path.concat(path, "member-"), data, CreateMode.EPHEMERAL_SEQUENTIAL);
        //得到节点名称
        String childName = childFullPath.substring(path.length() + 1);
        node.set(childName);
        if (logger.isInfoEnabled()) {
            logger.info("added EPHEMERAL_SEQUENTIAL path" + childFullPath);
        }
    }

    @Override
    protected void elect() throws Exception {
        //得到当前节点数据，并注册Watcher，默认节点已经排序
        List<PathData> children = zkClient.getSortedChildData(path, updateWatcher);

        // 比较数据是否发生变更
        Map<String, PathData> current = new HashMap<String, PathData>();
        Map<String, PathData> last = cache;
        boolean changed = false;
        PathData old;

        // 遍历当前数据，判断是否发生变更
        for (PathData child : children) {
            current.put(child.getPath(), child);
            old = last.get(child.getPath());
            if (old == null || !Arrays.equals(old.getData(), child.getData())) {
                changed = true;
            }
        }
        if (!changed && last.size() != current.size()) {
            changed = true;
        }

        if (changed) {
            if (children.size() >= 1) {
                String leaderName = children.get(0).getPath();
                if (!leader.get() && leaderName.equals(node.get())) {
                    // 以前不是leader，现在是leader
                    leader.set(true);
                }
            }

            cache = current;
            events.add(new ClusterEvent(path, children));
        }
    }

    @Override
    protected void onLostEvent() {
        writeLock.lock();
        try {
            if (leader.compareAndSet(true, false)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("lost leader." + Path.concat(path, node.get()));
                }
            }
            // 丢失连接，清理缓存的节点数据
            cache = new HashMap<String, PathData>();
            events.add(new ClusterEvent(path, new ArrayList<PathData>()));
        } finally {
            writeLock.unlock();
        }
    }
}
