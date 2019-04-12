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

import com.jd.journalq.registry.listener.ConnectionEvent;
import com.jd.journalq.registry.zookeeper.ZKClient;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.registry.listener.ConnectionListener;
import com.jd.journalq.registry.listener.LiveListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 监听器管理
 *
 * @param <L>
 * @param <E>
 * @author 朱妙文，何小锋
 */
public abstract class ListenerManager<L extends EventListener<E>, E> extends Service {
    private static final Logger logger = LoggerFactory.getLogger(ListenerManager.class);
    //Zookeeper客户端
    protected ZKClient zkClient;
    //路径
    protected String path;
    //事件监听器
    protected EventBus<E> events = new MyEventBus<E>();
    //更新事件监听器
    protected EventBus<UpdateType> updateEvents = new EventBus<UpdateType>("update_zk", new UpdateListener());
    //更新事件监听器
    protected EventBus<ConnectionEvent> connectionEvents =
            new EventBus<ConnectionEvent>("cnn_events", new MyConnectionListener());
    //连接监听器
    protected ConnectionWatcher connectionWatcher = new ConnectionWatcher();
    //更新监听器,多次注册同一个实例时，只会触发一次watcher
    protected UpdateWatcher updateWatcher = new UpdateWatcher();
    //计算器
    protected AtomicLong updateCounter = new AtomicLong(0);

    public ListenerManager(ZKClient zkClient, String path) {
        if (zkClient == null) {
            throw new IllegalArgumentException("zkClient is null");
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path is null");
        }
        this.zkClient = zkClient;
        this.path = path;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        events.start();
        connectionEvents.start();

        // 随机时间间隔，减少同时访问zookeeper的压力
        updateEvents.setInterval(1000 + (long) (1000 * Math.random()));
        // 随机空闲时间(1分钟-5分钟),触发空闲事件更新数据，防止zookeeper丢失事件
        events.setIdleTime(60 * 1000 + (long) (5 * 60 * 1000 * Math.random()));
        updateEvents.start();

        // 注册connection manager
        zkClient.addListener(connectionWatcher);
    }

    @Override
    protected void doStop() {
        zkClient.removeListener(connectionWatcher);
        connectionEvents.stop();
        updateEvents.stop();
        // 尽量不要使用true参数，因为事件处理器可能使用锁，造成死锁
        events.stop();
        updateCounter.set(0);
        super.doStop();
    }

    /**
     * 添加了监听器，发布初始化事件给指定监听器.<br/>
     * 该方法在读锁里面
     *
     * @param listener 监听器
     */
    protected void onAddListener(final L listener) {

    }

    /**
     * 添加监听器
     *
     * @param listener 监听器
     * @return true 如果添加成功
     */
    public boolean addListener(final L listener) {
        if (listener == null) {
            return false;
        }
        readLock.lock();
        try {
            // 必须在锁里面，避免在更新数据，重复发送
            boolean result = events.addListener(listener);
            if (isStarted()) {
                onAddListener(listener);
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 删除监听器.<br/>
     * 该方法在读锁里面
     *
     * @param listener 监听器
     */
    protected void onRemoveListener(final L listener) {

    }

    /**
     * 移除监听器
     *
     * @param listener 监听器
     */
    public void removeListener(final L listener) {
        if (listener == null) {
            return;
        }
        readLock.lock();
        try {
            if (events.removeListener(listener)) {
                if (isStarted()) {
                    onRemoveListener(listener);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 连接事件处理
     */
    protected void onConnectedEvent() {
        //触发数据更新事件
        updateEvents.add(UpdateType.UPDATE);
    }

    /**
     * 重新连接上，Session没有失效事件处理
     */
    protected void onReconnectedEvent() {

    }

    /**
     * 连接断开事件处理，Session没有失效
     */
    protected void onSuspendedEvent() {

    }

    /**
     * 连接断开事件处理，Session失效
     */
    protected void onLostEvent() {

    }

    /**
     * 数据更新事件
     *
     * @throws Exception
     */
    protected void onUpdateEvent() throws Exception {

    }

    /**
     * 数据更新类型
     */
    public enum UpdateType {
        UPDATE
    }

    /**
     * 连接监听器
     */
    protected class ConnectionWatcher implements ConnectionListener {
        @Override
        public void onEvent(final ConnectionEvent event) {
            // 异步处理，避免阻塞后续的Zookeeper连接事件
            connectionEvents.add(event);
        }
    }

    /**
     * 数据更新事件派发器
     */
    protected class UpdateListener implements EventListener<UpdateType> {

        @Override
        public void onEvent(final UpdateType event) {
            try {
                //转换成通知事件
                if (isStarted() && zkClient.isConnected()) {
                    updateCounter.incrementAndGet();
                    onUpdateEvent();
                }
            } catch (KeeperException.ConnectionLossException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.MarshallingErrorException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.UnimplementedException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.BadArgumentsException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.APIErrorException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.NoAuthException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.BadVersionException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.AuthFailedException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.InvalidACLException e) {
                logger.error("error occurs when update data ", e);
            } catch (KeeperException.SessionExpiredException e) {
                logger.error("error occurs when update data ", e);
            } catch (InterruptedException ignored) {
                //中断
            } catch (Throwable e) {
                // 必须捕获Throwable，否则调度会终止
                updateEvents.add(UpdateType.UPDATE);
                logger.warn("error occurs when update data ", e);
                await(1000);
            }
        }
    }

    /**
     * 路径监听器
     */
    protected class UpdateWatcher implements Watcher {
        @Override
        public void process(final WatchedEvent event) {
            // None类型会在Session expired / connection loss/  auth failed得到对应的触发，对应的触发path为null
            // 针对出现None的类型，会将所有的watcher进行触发，同时并不会移除watcher
            // 所以，watcher会在下一次reconnect成功后再次触发，除非设置DisableAutoResetWatch
            if (event.getType() != Event.EventType.None) {
                updateEvents.add(UpdateType.UPDATE);
            }
        }
    }

    /**
     * 通知监听器的事件总线
     *
     * @param <E>
     */
    protected class MyEventBus<E> extends EventBus<E> {
        public MyEventBus() {
            super("events");
        }

        @Override
        protected void publish(final Ownership event) {
            // 过滤掉存活的初始化事件
            if (event.owner != null && (event.owner instanceof LiveListener) && updateCounter.get() <= 1) {
                return;
            }
            super.publish(event);
        }

        @Override
        protected void onIdle() {
            // 空闲事件，则重新更新一下数据，防止zookeeper丢失事件
            if (zkClient.isConnected()) {
                updateEvents.add(UpdateType.UPDATE);
            }
            super.onIdle();
        }
    }

    /**
     * 连接监听器
     */
    protected class MyConnectionListener implements EventListener<ConnectionEvent> {
        @Override
        public void onEvent(final ConnectionEvent event) {
            switch (event.getType()) {
                case CONNECTED://连接上
                    onConnectedEvent();
                    break;
                case RECONNECTED://重连上
                    onReconnectedEvent();
                    break;
                case FAILED://失败
                case LOST://连接断开
                    onLostEvent();
                    break;
                case SUSPENDED://连接挂起
                    onSuspendedEvent();
                    break;
            }
        }
    }
}