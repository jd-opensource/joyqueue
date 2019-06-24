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

import com.jd.joyqueue.registry.listener.LeaderEvent;
import com.jd.joyqueue.registry.util.Path;
import com.jd.joyqueue.registry.zookeeper.ZKClient;
import com.jd.joyqueue.registry.listener.LeaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Leader管理
 *
 * @author 何小锋
 */
public class LeaderManager extends ElectionManager<LeaderListener, LeaderEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LeaderManager.class);

    public LeaderManager(ZKClient zkClient, String path) {
        super(zkClient, path);
    }

    @Override
    protected void onLostEvent() {
        writeLock.lock();
        try {
            if (leader.compareAndSet(true, false)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("lost leader." + Path.concat(path, node.get()));
                }
                // 对外发布失去领导事件
                events.add(new LeaderEvent(LeaderEvent.LeaderEventType.LOST, path));
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected void onAddListener(LeaderListener listener) {
        if (node.get() == null) {
            //初始化创建Leader选举的临时节点
            updateEvents.add(UpdateType.UPDATE);
        } else if (leader.get()) {
            //当前已经是Leader，广播Take事件给该监听器进行初始化
            events.add(new LeaderEvent(LeaderEvent.LeaderEventType.TAKE, path), listener);
        }
    }

    @Override
    protected void elect() throws Exception {
        //得到当前节点数据，并注册Watcher
        List<String> children = zkClient.getSortedChildren(path, updateWatcher);
        if (children.isEmpty()) {
            events.add(new LeaderEvent(LeaderEvent.LeaderEventType.LOST, path));
        } else {
            String leaderName = children.get(0);
            if (leader.get() && !leaderName.equals(node.get())) {
                // 以前是leader，现在不是leader
                events.add(new LeaderEvent(LeaderEvent.LeaderEventType.LOST, path));
                leader.set(false);
                if (logger.isDebugEnabled()) {
                    logger.debug("lost leader." + Path.concat(path, leaderName));
                }
            } else if (!leader.get() && leaderName.equals(node.get())) {
                // 以前不是leader，现在是leader
                events.add(new LeaderEvent(LeaderEvent.LeaderEventType.TAKE, path));
                leader.set(true);
                if (logger.isDebugEnabled()) {
                    logger.debug("take leader." + Path.concat(path, leaderName));
                }
            }
        }
    }

}