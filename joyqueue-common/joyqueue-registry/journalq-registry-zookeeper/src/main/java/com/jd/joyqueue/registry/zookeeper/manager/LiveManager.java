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
import com.jd.joyqueue.registry.listener.ConnectionEvent;
import com.jd.joyqueue.toolkit.concurrent.EventListener;
import com.jd.joyqueue.registry.listener.ConnectionListener;
import com.jd.joyqueue.registry.zookeeper.ZKClient;
import com.jd.joyqueue.toolkit.concurrent.EventBus;
import com.jd.joyqueue.toolkit.service.Service;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 存活管理
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-19 上午9:29
 */
public class LiveManager extends Service {
    private static final Logger logger = LoggerFactory.getLogger(LiveManager.class);
    // Zookeeper客户端
    protected ZKClient zkClient;
    // 存活的节点
    protected Set<PathData> lives = new CopyOnWriteArraySet<PathData>();
    // 待删除节点
    protected Set<PathData> removes = new CopyOnWriteArraySet<PathData>();
    // 连接监听器
    protected ConnectionWatcher connectionWatcher = new ConnectionWatcher();
    // 存活节点事件
    protected EventBus<LiveEvent> events;

    public LiveManager(final ZKClient zkClient) {
        if (zkClient == null) {
            throw new IllegalArgumentException("zkClient is null");
        }
        this.zkClient = zkClient;
        this.events = new EventBus<LiveEvent>(new LiveListener());
        this.events.setTimeout(5000);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        events.start();
        // 注册connection manager
        zkClient.addListener(connectionWatcher);
    }

    @Override
    protected void doStop() {
        zkClient.removeListener(connectionWatcher);
        // 尝试删除存活节点
        if (zkClient.isConnected()) {
            for (PathData data : lives) {
                try {
                    zkClient.delete(data.getPath());
                } catch (Throwable ignored) {
                }
            }
        }
        // 带参数会造成死锁，监听器使用了readLock
        events.stop();
        // 不清理存活节点，还可以重新启动
        super.doStop();
    }

    /**
     * 添加存活节点
     *
     * @param data 存活节点
     */
    public void addLive(final PathData data) {
        if (data == null) {
            return;
        }
        // 写锁
        writeLock.lock();
        try {
            if (lives.add(data)) {
                removes.remove(data);
                if (isStarted()) {
                    events.add(new LiveEvent(LiveEventType.CREATE, data));
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 删除存活节点
     *
     * @param data 存活节点
     */
    public void deleteLive(final PathData data) {
        if (data == null) {
            return;
        }
        // 写锁
        writeLock.lock();
        try {
            if (lives.remove(data)) {
                removes.add(data);
                if (isStarted()) {
                    events.add(new LiveEvent(LiveEventType.DELETE, data));
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 连接监听器
     */
    protected class ConnectionWatcher implements ConnectionListener {
        @Override
        public void onEvent(final ConnectionEvent event) {
            if (event.getType() == ConnectionEvent.ConnectionEventType.CONNECTED) {
                readLock.lock();
                try {
                    for (PathData data : lives) {
                        events.add(new LiveEvent(LiveEventType.CREATE, data));
                    }
                } finally {
                    readLock.unlock();
                }
            }
        }
    }

    protected class LiveListener implements EventListener<LiveEvent>, EventListener.Heartbeat {
        @Override
        public void onEvent(final LiveEvent event) {
            try {
                if (event == null) {
                    // 心跳事件，目前是5秒，检查一下存活节点是否还在了
                    onHeartbeat();
                } else if (event.getType() == LiveEventType.CREATE) {
                    onCreate(event.data);
                } else if (event.getType() == LiveEventType.DELETE) {
                    onDelete(event.data);
                }
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public boolean trigger(final long now) {
            // 默认5秒
            return true;
        }

        /**
         * 删除存活节点
         *
         * @param live 节点
         */
        protected void onDelete(final PathData live) {
            if (!isStarted() || lives.contains(live)) {
                return;
            }
            // 删除节点
            if (zkClient.isConnected()) {
                try {
                    zkClient.delete(live.getPath());
                    removes.remove(live);
                } catch (Exception ignored) {
                    // 删除失败后忽略，会在心跳的时候进行删除
                    logger.warn("delete live path error." + live.getPath() + ", retry....");
                }
            }
        }

        /**
         * 创建存活节点
         *
         * @param live
         */
        protected void onCreate(final PathData live) {
            if (!isStarted() || !lives.contains(live)) {
                return;
            }
            //确保Zookeeper连接上，任务没有被删除
            try {
                zkClient.create(live.getPath(), live.getData(), CreateMode.EPHEMERAL);
            } catch (Exception e) {
                // 出错后会在5秒心跳里面重建
                logger.warn("create live path error." + live.getPath() + ", retry....");
            }
        }

        /**
         * 心跳事件
         */
        protected void onHeartbeat() {
            if (!isStarted() || !zkClient.isConnected() || lives.isEmpty()) {
                return;
            }
            createLives();
            deleteLives();
        }

        /**
         * 检查待删除的节点是否都删除了，如果没有删除，则删除
         */
        protected void deleteLives() {
            // 删除待删除节点
            List<PathData> success = new ArrayList<PathData>();
            for (PathData remove : removes) {
                if (isStarted() && zkClient.isConnected()) {
                    try {
                        if (zkClient.exists(remove.getPath())) {
                            zkClient.delete(remove.getPath());
                        }
                        success.add(remove);
                    } catch (Exception e) {
                        logger.warn("delete remove path error." + remove.getPath() + ", retry....");
                    }
                }
            }
            if (!success.isEmpty()) {
                removes.removeAll(success);
            }
        }

        /**
         * 检测存活节点是否都创建了，如果没有创建，则创建
         */
        protected void createLives() {
            // 创建存活节点
            for (PathData live : lives) {
                if (isStarted() && zkClient.isConnected()) {
                    try {
                        if (!zkClient.exists(live.getPath())) {
                            zkClient.create(live.getPath(), live.getData(), CreateMode.EPHEMERAL);
                        }
                    } catch (Exception e) {
                        logger.warn("create live path error." + live.getPath() + ", retry....");
                    }
                }
            }
        }
    }

    /**
     * 存活节点事件
     */
    protected static class LiveEvent {
        // 类型
        protected LiveEventType type;
        // 节点
        protected PathData data;

        public LiveEvent(LiveEventType type, PathData data) {
            this.type = type;
            this.data = data;
        }

        public LiveEventType getType() {
            return type;
        }

        public PathData getData() {
            return data;
        }
    }

    /**
     * 存活事件类型
     */
    protected enum LiveEventType {
        /**
         * 创建存活节点
         */
        CREATE,
        /**
         * 删除存活节点
         */
        DELETE
    }
}
