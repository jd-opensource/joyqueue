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
package com.jd.journalq.registry.zookeeper;

import com.jd.journalq.registry.PathData;
import com.jd.journalq.registry.listener.ConnectionEvent;
import com.jd.journalq.toolkit.URL;
import com.jd.journalq.toolkit.exception.Abnormity;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.lang.Online;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import com.jd.journalq.toolkit.time.SystemClock;
import com.jd.journalq.registry.RegistryException;
import com.jd.journalq.registry.listener.ConnectionListener;
import com.jd.journalq.registry.util.Path;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.retry.Retry;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * zookeeper客户端
 *
 * @author 朱妙文，何小锋
 */
public class ZKClient implements LifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(ZKClient.class);
    //保留当前的SessionID
    protected final AtomicLong zkSessionId = new AtomicLong();
    //zookeeper的连接URL
    protected URL url;
    //zookeeper对象
    protected volatile ZooKeeper zooKeeper;
    //用于多注册中心时手动控制重连次数
    protected int connectionRetryTimes = 0;
    //重试次数
    protected int retryTimes = 1;
    //重试间隔
    protected int retryInterval = 1000;
    //会话超时
    protected int sessionTimeout = 30000;
    //是否能从Observe节点读取
    protected boolean canBeReadOnly = false;
    //是否关闭
    protected AtomicBoolean started = new AtomicBoolean(false);
    protected AtomicBoolean connected = new AtomicBoolean(false);
    //连接成功
    protected BlockingQueue<ConnectionState> mailbox = new ArrayBlockingQueue<ConnectionState>(1);
    //连接事件
    protected EventBus<ConnectionEvent> connectionEvents;
    //连接请求管理器
    protected EventBus<ConnectionTask> connectionTasks;

    public ZKClient() {
        this(null, null);
    }

    public ZKClient(URL url, List<ConnectionListener> listeners) {
        this.url = url;
        this.connectionEvents = new EventBus<ConnectionEvent>("zk_cnn_event", listeners);
        this.connectionTasks = new EventBus<ConnectionTask>("zk_cnn_task", new TaskListener());
    }

    @Override
    public void start() throws Exception {
        if (url == null) {
            throw new IllegalStateException("url can not be null.");
        }
        if (started.compareAndSet(false, true)) {
            connectionRetryTimes = url.getInteger("connectionRetryTimes", connectionRetryTimes);
            retryTimes = url.getInteger("retryTimes", retryTimes);
            retryInterval = url.getPositive("retryInterval", retryInterval);
            sessionTimeout = url.getPositive("sessionTimeout", sessionTimeout);
            canBeReadOnly = url.getBoolean("canBeReadOnly", canBeReadOnly);

            // 连接事件管理器
            connectionEvents.start();
            // 连接任务
            connectionTasks.start();
            // 增加连接任务
            connectionTasks.add(new ConnectionTask());
        }
    }

    @Override
    public void stop() {
        if (started.compareAndSet(true, false)) {
            connected.set(false);
            //关闭zookeeper
            disconnect();
            connectionEvents.stop();
            connectionTasks.stop();
        }
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    /**
     * 关闭zookeeper
     */
    protected void disconnect() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * 检查状态
     */
    protected void checkState() throws KeeperException {
        if (zooKeeper == null) {
            throw new KeeperException.ConnectionLossException();
        }
        if (connected.get()) {
            return;
        }
        States states = zooKeeper.getState();
        if (states == States.CONNECTED) {
            return;
        } else if (states == States.AUTH_FAILED) {
            throw new KeeperException.AuthFailedException();
        }
        throw new KeeperException.ConnectionLossException();
    }

    /**
     * 创建节点
     *
     * @param path       路径
     * @param data       数据
     * @param createMode 创建模式
     * @return 节点路径
     * @throws Exception
     */
    public String create(final String path, final byte[] data, final CreateMode createMode) throws Exception {
        if (path == null) {
            return null;
        }
        return Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    //检查状态
                    checkState();
                    //确保父节点创建好了
                    createDirectory(Path.parent(path));
                    return zooKeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, createMode);
                } catch (NodeExistsException e) {
                    if (createMode == CreateMode.EPHEMERAL || createMode == CreateMode.EPHEMERAL_SEQUENTIAL) {
                        Stat stat = new Stat();
                        zooKeeper.getData(path, null, stat);
                        if (stat.getEphemeralOwner() != zkSessionId.get()) {
                            //sessionId已经发生变化
                            zooKeeper.delete(path, -1);
                            zooKeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, createMode);
                        }
                    } else if (data != null && data.length > 0) {
                        update(path, data);
                    }
                    return path;
                }
            }
        });

    }

    /**
     * 创建多个节点
     *
     * @param paths 节点列表
     * @throws Exception
     */
    public void create(final List<String> paths) throws Exception {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
            @Override
            public Void call() throws Exception {
                //检查状态
                checkState();
                //循环创建节点
                for (String path : paths) {
                    create(path, null, CreateMode.PERSISTENT);
                }
                return null;
            }
        });
    }

    /**
     * 更新数据
     *
     * @param path 路径
     * @param data 数据
     * @throws Exception
     */
    public void update(String path, byte[] data) throws Exception {
        update(path, data, -1);
    }

    /**
     * 更新数据
     *
     * @param path   路径
     * @param data   数据
     * @param parent 父节点数据
     * @throws Exception
     */
    public void update(final String path, final byte[] data, final byte[] parent) throws Exception {
        if (path == null || path.isEmpty()) {
            return;
        }
        Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
            @Override
            public Void call() throws Exception {
                //检查状态
                checkState();
                //创建节点
                createDirectory(path);
                //设置节点数据
                if (parent != null) {
                    zooKeeper.setData(path, data, -1);
                    zooKeeper.setData(Path.parent(path), parent, -1);
                } else {
                    zooKeeper.setData(path, data, -1);
                }
                return null;
            }
        });
    }

    /**
     * 更新数据
     *
     * @param path    路径
     * @param data    数据
     * @param version 数据版本号
     * @throws Exception
     */
    public void update(final String path, final byte[] data, final int version) throws Exception {
        if (path == null || path.isEmpty()) {
            return;
        }
        try {
            Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
                @Override
                public Void call() throws Exception {
                    //检查状态
                    checkState();
                    //创建节点
                    createDirectory(path);
                    //设置节点数据
                    zooKeeper.setData(path, data, version);
                    return null;
                }
            });
        } catch (KeeperException.BadVersionException e) {
            throw new RegistryException.BadVersionException(e.getMessage(), e);
        }
    }

    /**
     * 删除路径
     *
     * @param path 路径
     * @throws Exception
     */
    public void delete(final String path) throws Exception {
        if (path == null || path.isEmpty()) {
            return;
        }
        Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
            @Override
            public Void call() throws Exception {
                //检查状态
                checkState();
                try {
                    //删除节点
                    zooKeeper.delete(path, -1);
                } catch (NoNodeException e) {
                    // ignore
                }
                return null;
            }
        });
    }

    /**
     * 删除多个路径
     *
     * @param paths 路径集合
     * @throws Exception
     */
    public void delete(final List<String> paths) throws Exception {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
            @Override
            public Void call() throws Exception {
                //检查状态
                checkState();
                //循环删除节点
                for (String path : paths) {
                    delete(path);
                }
                return null;
            }
        });
    }

    /**
     * 递归删除路径
     *
     * @param path 路径
     * @throws Exception
     */
    public void deleteRecursive(final String path) throws Exception {
        if (path == null || path.isEmpty()) {
            return;
        }
        Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
            @Override
            public Void call() throws Exception {
                //检查状态
                checkState();
                Path.validate(path);
                //遍历删除子节点
                List<String> tree = children(path);
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleting " + tree);
                    logger.debug("Deleting " + tree.size() + " subnodes ");
                }
                for (int i = tree.size() - 1; i >= 0; --i) {
                    try {
                        // Delete the leaves first and eventually get rid of the root
                        // Delete all versions of the node with -1.
                        zooKeeper.delete(tree.get(i), -1);
                    } catch (NoNodeException e) {
                        // ignore
                    }
                }
                return null;
            }
        });
    }

    /**
     * 获取数据
     *
     * @param path 路径
     * @return 数据
     * @throws Exception
     */
    public PathData getData(final String path) throws Exception {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return getData(path, null, new Stat());
    }

    /**
     * 获取数据
     *
     * @param path 路径
     * @param stat 状态数据
     * @return 数据
     * @throws Exception
     */
    public PathData getData(final String path, final Stat stat) throws Exception {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return getData(path, null, stat);
    }

    /**
     * 获取数据
     *
     * @param path    路径
     * @param watcher 监听器
     * @return 数据
     * @throws Exception
     */
    public PathData getData(String path, Watcher watcher) throws Exception {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return getData(path, watcher, new Stat());
    }

    /**
     * 获取数据
     *
     * @param path    路径
     * @param watcher 监听器
     * @param stat    stat
     * @return PathData
     * @throws Exception
     */
    public PathData getData(final String path, final Watcher watcher, final Stat stat) throws Exception {
        return Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<PathData>() {
            @Override
            public PathData call() throws Exception {
                //检查状态
                checkState();
                byte[] data = null;
                if (watcher == null) {
                    //没有监听器，直接获取数据
                    try {
                        data = zooKeeper.getData(path, false, stat);
                    } catch (NoNodeException e) {
                        //ignore
                    }
                } else {
                    //有监听器，先创建节点
                    createDirectory(path);
                    data = zooKeeper.getData(path, watcher, stat);
                }
                if (stat != null) {
                    return new PathData(Path.node(path), data, stat.getVersion());
                }
                return new PathData(Path.node(path), data);
            }
        });
    }

    /**
     * 获取子节点数据
     *
     * @param path 路径
     * @return 子节点数据集合
     * @throws Exception
     */
    public List<PathData> getChildData(final String path) throws Exception {
        return getChildData(path, null);
    }

    /**
     * 获取子节点数据
     *
     * @param path    路径
     * @param watcher 监听器
     * @return 子节点数据集合
     * @throws Exception
     */
    public List<PathData> getChildData(final String path, final Watcher watcher) throws Exception {
        List<String> children = getChildren(path, watcher);
        if (children == null || children.isEmpty()) {
            return new ArrayList<PathData>();
        }
        List<PathData> result = new ArrayList<PathData>(children.size());
        for (String child : children) {
            result.add(getData(Path.concat(path, child)));
        }
        return result;
    }

    /**
     * 获取排序的子节点数据
     *
     * @param path 路径
     * @return 子节点数据集合
     * @throws Exception
     */
    public List<PathData> getSortedChildData(String path) throws Exception {
        return getSortedChildData(path, null);
    }

    /**
     * 获取排序的子节点数据
     *
     * @param path    路径
     * @param watcher 监听器
     * @return 子节点数据集合
     * @throws Exception
     */
    public List<PathData> getSortedChildData(final String path, final Watcher watcher) throws Exception {
        List<String> children = getChildren(path, watcher);
        if (children == null || children.size() < 1) {
            return new ArrayList<PathData>();
        }
        Collections.sort(children);
        List<PathData> result = new ArrayList<PathData>(children.size());
        for (String childPath : children) {
            result.add(getData(Path.concat(path, childPath)));
        }
        return result;
    }

    /**
     * 获取排序的子节点
     *
     * @param path 路径
     * @return 子节点
     * @throws Exception
     */
    public List<String> getSortedChildren(final String path) throws Exception {
        return getSortedChildren(path, null);
    }

    /**
     * 获取排序的子节点
     *
     * @param path    路径
     * @param watcher 监听器
     * @return 子节点
     * @throws Exception
     */
    public List<String> getSortedChildren(final String path, final Watcher watcher) throws Exception {
        List<String> children = getChildren(path, watcher);
        Collections.sort(children);
        return children;
    }

    /**
     * 获取子节点
     *
     * @param path 路径
     * @return 子节点
     * @throws Exception
     */
    public List<String> getChildren(final String path) throws Exception {
        return getChildren(path, null);
    }

    /**
     * 获取子节点，并设置监听器
     *
     * @param path    路径
     * @param watcher 监听器
     * @return 子节点
     * @throws Exception
     */
    public List<String> getChildren(final String path, final Watcher watcher) throws Exception {
        if (path == null || path.isEmpty()) {
            return new ArrayList<String>();
        }
        return Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                //检查状态
                checkState();
                List<String> children = null;
                //判断是否有监听器
                if (watcher != null) {
                    //有监听器，则必须创建节点
                    createDirectory(path);
                    //获取子节点数据，并设置监听器
                    children = zooKeeper.getChildren(path, watcher);
                } else {
                    //没有监听器
                    try {
                        children = zooKeeper.getChildren(path, false);
                    } catch (NoNodeException e) {
                        //ignore
                    }
                }
                return children == null ? new ArrayList<String>() : children;
            }
        });
    }

    /**
     * 得到当前的SessionId
     *
     * @return 当前的SessionId
     */
    public long getSessionId() {
        return zkSessionId.get();
    }

    /**
     * 判断节点是否存在
     *
     * @param path 路径
     * @return 节点是否存在
     * @throws Exception
     */
    public boolean exists(final String path) throws Exception {
        return exists(path, null);
    }

    /**
     * 判断节点是否存在
     *
     * @param path    路径
     * @param watcher 监听器
     * @return 节点是否存在
     * @throws Exception
     */
    public boolean exists(final String path, final Watcher watcher) throws Exception {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                //检查状态
                checkState();
                if (watcher == null) {
                    return (zooKeeper.exists(path, false) != null);
                }
                return zooKeeper.exists(path, watcher) != null;
            }
        });
    }

    /**
     * 增加连接监听器
     *
     * @param listener 连接监听器
     */
    public void addListener(final ConnectionListener listener) {
        if (connectionEvents.addListener(listener)) {
            if (isConnected()) {
                connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.CONNECTED, url), listener);
            }
        }
    }

    /**
     * 删除连接监听器
     *
     * @param listener 连接监听器
     */
    public void removeListener(ConnectionListener listener) {
        connectionEvents.removeListener(listener);
    }

    /**
     * 创建节点
     *
     * @param path 路径
     * @throws Exception
     */
    protected void createDirectory(final String path) throws Exception {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (exists(path)) {
            //路径已经存在
            return;
        }
        Retry.execute(new RetryPolicy(retryInterval, retryTimes), new ZKCallable<Void>() {
            @Override
            public Void call() throws Exception {
                checkState();
                Path.validate(path);

                int pos = 1; // skip first slash, root is guaranteed to exist
                do {
                    pos = path.indexOf('/', pos + 1);
                    if (pos == -1) {
                        pos = path.length();
                    }

                    String subPath = path.substring(0, pos);
                    if (!exists(subPath)) {
                        try {
                            zooKeeper.create(subPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        } catch (KeeperException.NodeExistsException e) {
                            // ignore... someone else has created it since we checked
                        }
                    }

                } while (pos < path.length());
                return null;
            }
        });
    }

    /**
     * 是否连接上
     *
     * @return 是否连接上
     */
    public boolean isConnected() {
        if (zooKeeper == null) {
            return false;
        }
        return connected.get();
    }

    /**
     * 宽度优先遍历子树
     *
     * @param path 子树的跟节点
     * @throws InterruptedException
     * @throws KeeperException
     */
    protected List<String> children(final String path) throws KeeperException, InterruptedException {
        Deque<String> queue = new LinkedList<String>();
        List<String> tree = new ArrayList<String>();
        queue.add(path);
        tree.add(path);
        while (true) {
            String node = queue.pollFirst();
            if (node == null) {
                break;
            }
            List<String> children = zooKeeper.getChildren(node, false);
            for (final String child : children) {
                final String childPath = node + "/" + child;
                queue.add(childPath);
                tree.add(childPath);
            }
        }
        return tree;
    }

    /**
     * 重试回调
     *
     * @param <T>
     */
    protected abstract class ZKCallable<T> implements Callable<T>, Abnormity, Online {

        @Override
        public boolean isStarted() {
            return ZKClient.this.isStarted();
        }

        @Override
        public boolean onException(Throwable e) {
            if (e instanceof KeeperException) {
                KeeperException exception = (KeeperException) e;
                switch (exception.code()) {
                    case CONNECTIONLOSS:
                        return true;
                    case OPERATIONTIMEOUT:
                        return true;
                    case SESSIONMOVED:
                        return true;
                    case SESSIONEXPIRED:
                        return true;

                }
            }
            return false;
        }
    }

    /**
     * 连接状态
     */
    protected enum ConnectionState {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 超时失败
         */
        TIMEOUT,
    }

    /**
     * 连接类型
     */
    protected enum ConnectionType {
        /**
         * 连接
         */
        CONNECTION,
        /**
         * 等到自动重连
         */
        RECONNECTION
    }

    /**
     * 连接请求
     */
    protected class ConnectionTask {
        //任务创建时间
        protected long createTime;
        //连接类型
        protected ConnectionType type;

        public ConnectionTask() {
            this(ConnectionType.CONNECTION);
        }

        public ConnectionTask(ConnectionType type) {
            this.createTime = SystemClock.now();
            this.type = type;
        }
    }

    /**
     * 连接监听器
     */
    protected class ConnectionWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            //已经关闭了
            if (!isStarted()) {
                return;
            }
            //处理连接事件
            if (logger.isDebugEnabled()) {
                logger.debug("Zookeeper event: " + event);
            }
            //确保zookeeper被赋值
            switch (event.getState()) {
                case ConnectedReadOnly:// 只读连接
                case SyncConnected:// 连接
                    connected.set(true);
                    onSyncConnected();
                    break;
                case Disconnected:// 断开连接，需要恢复连接
                    connected.set(false);
                    onDisconnected();
                    break;
                case Expired:// 会话过期，需要新建连接
                    connected.set(false);
                    onExpired();
                    break;
                case AuthFailed:// 认证失败
                case SaslAuthenticated:// 认证失败
                    connected.set(false);
                    onAuthFailed();
                    break;
                default:
                    connected.set(false);
                    onExpired();
                    break;
            }
        }

        /**
         * 连接断开
         */
        protected void onDisconnected() {
            // Disconnected -- zookeeper library will handle reconnects
            logger.info("Disconnected from Zookeeper,publishing suspended event.");
            connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.SUSPENDED, url));
            // 尝试重新恢复连接
            connectionTasks.add(new ConnectionTask(ConnectionType.RECONNECTION));
        }

        /**
         * 会话过期
         */
        protected void onExpired() {
            logger.info("Zookeeper session is expired,publishing lost event.");
            connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.LOST, url));
            // 会话过期，新建连接
            connectionTasks.add(new ConnectionTask(ConnectionType.CONNECTION));
        }

        /**
         * 会话过期
         */
        protected void onAuthFailed() {
            logger.info("Authenticated failed from Zookeeper, publishing lost event.");
            connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.LOST, url));
            // 新建连接
            connectionTasks.add(new ConnectionTask(ConnectionType.CONNECTION));
        }

        /**
         * 连接上
         */
        protected void onSyncConnected() {
            //连接上
            if (mailbox.offer(ConnectionState.SUCCESS)) {
                //调用线程没有超时关闭
                //获取SessionId
                long newSessionId = zooKeeper.getSessionId();
                long oldSessionId = zkSessionId.getAndSet(newSessionId);
                if (oldSessionId != newSessionId) {
                    logger.info("SyncConnected to Zookeeper,publishing connected event.");
                    //session超时，sessionId改变了，则发布CONNECTED事件
                    connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.CONNECTED, url));
                } else {
                    logger.info("Reconnected to Zookeeper,publishing reconnected event.");
                    //session没有超时，则发布RECONNECTED事件
                    connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.RECONNECTED, url));
                }
            }

        }
    }

    /**
     * 连接任务监听器，支持心跳，如果没有事件，默认会1秒钟执行一下
     */
    protected class TaskListener implements EventListener<ConnectionTask>, EventListener.Heartbeat {

        @Override
        public boolean trigger(long now) {
            // 默认1秒会触发一下心跳
            return true;
        }

        @Override
        public void onEvent(final ConnectionTask event) {
            if (!isStarted()) {
                return;
            }
            try {
                // 判断是否连接
                if (isConnected()) {
                    // 当前连接上
                    if (event != null) {
                        // Zookeeper会自动重连，等待1秒钟再次判断是否连接上，
                        Thread.sleep(1000);
                        if (isConnected()) {
                            // 丢弃任务
                            logger.info("zookeeper is connected, discard the event");
                        } else if (isStarted()) {
                            // 重连
                            logger.info("Attempting to connect to zookeeper servers " + url.getAddress());
                            retryConnect(event);
                        }
                    }
                } else if (event == null) {
                    // 心跳时间，发现当前没有连接上，则添加一个连接任务
                    connectionTasks.add(new ConnectionTask());
                } else {
                    // 重连
                    logger.info("Attempting to connect to zookeeper servers " + url.getAddress());
                    retryConnect(event);
                }
            } catch (InterruptedException ignored) {
                //被中断了
            }
        }

        /**
         * 连接，出现异常并重试
         *
         * @param task 任务
         * @throws InterruptedException
         */
        protected void retryConnect(final ConnectionTask task) throws InterruptedException {
            if (task == null) {
                return;
            }
            //等待连接上
            long retryCount = 0;
            while (!Thread.currentThread().isInterrupted() && isStarted()) {
                if (connect(task)) {
                    return;
                }

                //重试次数加1
                retryCount++;
                //超过了指定的最大重试次数，则退出
                if (connectionRetryTimes > 0 && retryCount >= connectionRetryTimes) {
                    connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.FAILED, url));
                    break;
                }
                //休息指定的间隔
                Thread.sleep(retryInterval);
                //清除信号量
                mailbox.clear();
            }
        }

        /**
         * 连接
         *
         * @return 是否成功
         */
        protected boolean connect(final ConnectionTask task) {
            if (task == null) {
                return false;
            }
            if (!isStarted()) {
                return false;
            }
            if (isConnected()) {
                return true;
            }

            try {
                //新建连接
                if (task.type == ConnectionType.CONNECTION) {
                    disconnect();
                    logger.info("try to connect to zookeeper " + url.getAddress());
                    zooKeeper = new ZooKeeper(url.getAddress(), sessionTimeout, new ConnectionWatcher());
                }
                //等待连接成功通知
                ConnectionState state = mailbox.poll(sessionTimeout, TimeUnit.MILLISECONDS);
                if (!isStarted()) {
                    // 已经关闭了
                    return false;
                }
                if (state == ConnectionState.SUCCESS && isConnected()) {
                    //连接上
                    return true;
                }
                //超时连接
                if (!mailbox.offer(ConnectionState.TIMEOUT)) {
                    //连接上
                    state = mailbox.poll();
                    if (state == ConnectionState.SUCCESS && isConnected()) {
                        return true;
                    }
                }
                //判断是否Session失效了
                if (task.type == ConnectionType.RECONNECTION) {
                    //session失效，发通知
                    connectionEvents.add(new ConnectionEvent(ConnectionEvent.ConnectionEventType.LOST, url));
                    //修改类型为CONNECTION
                    task.type = ConnectionType.CONNECTION;
                }
                //关闭Zookeeper的自动连接
                disconnect();
                return false;
            } catch (IOException e) {
                //创建Zookeeper并连接出错
                logger.error("error connected to zookeeper servers," + url.toString(), e);
                disconnect();
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //condition被中断了，则关闭Zookeeper直接返回
                disconnect();
                return false;
            }

        }

    }

}