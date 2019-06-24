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
package com.jd.joyqueue.registry.zookeeper;

import com.jd.joyqueue.registry.PathData;
import com.jd.joyqueue.registry.Registry;
import com.jd.joyqueue.registry.RegistryException;
import com.jd.joyqueue.registry.listener.ChildrenChangeListener;
import com.jd.joyqueue.registry.listener.ChildrenDataListener;
import com.jd.joyqueue.registry.listener.ChildrenListener;
import com.jd.joyqueue.registry.listener.ClusterListener;
import com.jd.joyqueue.registry.listener.ConnectionListener;
import com.jd.joyqueue.registry.listener.LeaderListener;
import com.jd.joyqueue.registry.listener.PathListener;
import com.jd.joyqueue.registry.zookeeper.manager.ChildrenChangeManager;
import com.jd.joyqueue.registry.zookeeper.manager.ChildrenDataManager;
import com.jd.joyqueue.registry.zookeeper.manager.ChildrenManager;
import com.jd.joyqueue.registry.zookeeper.manager.ClusterManager;
import com.jd.joyqueue.registry.zookeeper.manager.ElectionManager;
import com.jd.joyqueue.registry.zookeeper.manager.LeaderManager;
import com.jd.joyqueue.registry.zookeeper.manager.ListenerManager;
import com.jd.joyqueue.registry.zookeeper.manager.LiveManager;
import com.jd.joyqueue.registry.zookeeper.manager.LockManager;
import com.jd.joyqueue.registry.zookeeper.manager.PathManager;
import com.jd.joyqueue.toolkit.URL;
import com.jd.joyqueue.toolkit.lang.Close;
import com.jd.joyqueue.toolkit.service.Service;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * Zookeeper注册中心
 *
 * @author 何小锋
 */
public class ZKRegistry extends Service implements Registry {
    protected final Map<String, LeaderManager> leaderManagers = new HashMap<String, LeaderManager>();
    protected final Map<String, ClusterManager> clusterManagers = new HashMap<String, ClusterManager>();
    protected final Map<String, PathManager> pathManagers = new HashMap<String, PathManager>();
    protected final Map<String, ChildrenManager> childrenManagers = new HashMap<String, ChildrenManager>();
    protected final Map<String, ChildrenChangeManager> childrenChangeManagers = new HashMap<String, ChildrenChangeManager>();
    protected final Map<String, ChildrenDataManager> childrenDataManagers = new HashMap<String, ChildrenDataManager>();
    protected ZKClient zkClient = new ZKClient();
    protected LiveManager liveManager = new LiveManager(zkClient);
    protected LockManager lockManager = new LockManager(zkClient);
    protected URL url;

    public ZKRegistry() {
    }

    public ZKRegistry(URL url) {
        this.url = url;
    }

    @Override
    public String type() {
        return "zookeeper";
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (url == null) {
            throw new IllegalArgumentException("url can not be null.");
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        zkClient.setUrl(url);
        zkClient.start();
        liveManager.start();
        lockManager.start();
        start(leaderManagers);
        start(pathManagers);
        start(childrenManagers);
        start(childrenDataManagers);
    }

    @Override
    protected void doStop() {
        Close.close(leaderManagers.values()).close(pathManagers.values()).close(childrenManagers.values())
                .close(childrenDataManagers.values()).close(liveManager).close(lockManager).close(zkClient);
        // 不清理监听器，还可以重新启动
        super.doStop();
    }

    @Override
    public boolean isConnected() {
        if (zkClient == null) {
            return false;
        }
        return zkClient.isConnected();
    }

    @Override
    public void create(final String path, final byte[] data) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return;
        }
        execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return zkClient.create(path, data, CreateMode.PERSISTENT);
            }
        });
    }

    @Override
    public void create(final List<String> paths) throws RegistryException {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                zkClient.create(paths);
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public void createLive(final String path, final byte[] data) {
        if (path == null || path.isEmpty()) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                liveManager.addLive(new PathData(path, data));
            }
        }, true);
    }

    @Override
    public void deleteLive(final String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                liveManager.deleteLive(new PathData(path, null));
            }
        }, true);
    }

    @Override
    public Lock createLock(final String path) {
        return lockManager.createLock(path);
    }

    @Override
    public void update(final String path, final byte[] data) throws RegistryException {
        this.update(path, data, null);
    }

    @Override
    public void update(final PathData data) throws RegistryException {
        if (data == null || data.getPath() == null || data.getPath().isEmpty()) {
            return;
        }
        execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                zkClient.update(data.getPath(), data.getData(), data.getVersion());
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public void update(final String path, final byte[] data, final byte[] parent) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return;
        }
        execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                zkClient.update(path, data, parent);
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public void delete(final String path) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return;
        }
        execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                zkClient.delete(path);
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public void delete(final List<String> paths) throws RegistryException {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                zkClient.delete(paths);
                return Boolean.TRUE;
            }
        });
    }

    @Override
    public boolean exists(final String path) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return zkClient.exists(path);
            }
        });
    }

    @Override
    public boolean isLeader(final String path) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (!isConnected()) {
                    return Boolean.FALSE;
                }
                ElectionManager leaderManager = leaderManagers.get(path);
                if (leaderManager == null) {
                    return Boolean.FALSE;
                }
                return leaderManager.isLeader();
            }
        }, false);
    }

    @Override
    public PathData getData(final String path) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return execute(new Callable<PathData>() {
            @Override
            public PathData call() throws Exception {
                return zkClient.getData(path);
            }
        });
    }

    @Override
    public List<PathData> getChildData(final String path) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return new ArrayList<PathData>();
        }
        return execute(new Callable<List<PathData>>() {
            @Override
            public List<PathData> call() throws Exception {
                return zkClient.getChildData(path);
            }
        });
    }

    @Override
    public List<String> getChildren(final String path) throws RegistryException {
        if (path == null || path.isEmpty()) {
            return new ArrayList<String>();
        }
        return execute(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return zkClient.getChildren(path);
            }
        });
    }

    @Override
    public void addListener(final String path, final ChildrenListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                ChildrenManager manager;
                synchronized (childrenManagers) {
                    manager = childrenManagers.get(path);
                    if (manager == null) {
                        manager = new ChildrenManager(zkClient, path);
                        childrenManagers.put(path, manager);
                    }
                }
                manager.addListener(listener);
                if (isStarted() && !manager.isStarted()) {
                    start(manager);
                }
            }
        }, false);
    }

    @Override
    public void addListener(final String path, final ChildrenChangeListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                ChildrenChangeManager manager;
                synchronized (childrenDataManagers) {
                    manager = childrenChangeManagers.get(path);
                    if (manager == null) {
                        manager = new ChildrenChangeManager(zkClient, path);
                        childrenChangeManagers.put(path, manager);
                    }
                }
                manager.addListener(listener);
                if (isStarted() && !manager.isStarted()) {
                    start(manager);
                }
            }
        }, false);
    }

    @Override
    public void addListener(final String path, final ChildrenDataListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                ChildrenDataManager manager;
                synchronized (childrenManagers) {
                    manager = childrenDataManagers.get(path);
                    if (manager == null) {
                        manager = new ChildrenDataManager(zkClient, path);
                        childrenDataManagers.put(path, manager);
                    }
                }
                manager.addListener(listener);
                if (isStarted() && !manager.isStarted()) {
                    start(manager);
                }
            }
        }, false);
    }

    @Override
    public void addListener(final String path, final PathListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                PathManager manager;
                synchronized (pathManagers) {
                    manager = pathManagers.get(path);
                    if (manager == null) {
                        manager = new PathManager(zkClient, path);
                        pathManagers.put(path, manager);
                    }
                }
                manager.addListener(listener);
                if (isStarted() && !manager.isStarted()) {
                    start(manager);
                }
            }
        }, false);
    }

    @Override
    public void addListener(final String path, final LeaderListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                LeaderManager manager;
                synchronized (leaderManagers) {
                    manager = leaderManagers.get(path);
                    if (manager == null) {
                        manager = new LeaderManager(zkClient, path);
                        leaderManagers.put(path, manager);
                    }
                }
                manager.addListener(listener);
                if (isStarted() && !manager.isStarted()) {
                    start(manager);
                }
            }
        }, false);
    }

    @Override
    public void addListener(final String path, final ClusterListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                ClusterManager manager;
                synchronized (leaderManagers) {
                    manager = clusterManagers.get(path);
                    if (manager == null) {
                        manager = new ClusterManager(zkClient, path, listener.getIdentity());
                        clusterManagers.put(path, manager);
                    }
                }
                manager.addListener(listener);
                if (isStarted() && !manager.isStarted()) {
                    start(manager);
                }
            }
        }, false);
    }

    @Override
    public void addListener(final ConnectionListener listener) {
        if (listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                zkClient.addListener(listener);
            }
        }, false);
    }

    @Override
    public void removeListener(final String path, final PathListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                synchronized (pathManagers) {
                    PathManager manager = pathManagers.get(path);
                    if (manager != null) {
                        manager.removeListener(listener);
                    }
                }
            }
        }, false);
    }

    @Override
    public void removeListener(final String path, final ChildrenListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                synchronized (childrenManagers) {
                    ChildrenManager manager = childrenManagers.get(path);
                    if (manager != null) {
                        manager.removeListener(listener);
                    }
                }
            }
        }, false);
    }

    @Override
    public void removeListener(final String path, final ChildrenChangeListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                synchronized (childrenChangeManagers) {
                    ChildrenChangeManager manager = childrenChangeManagers.get(path);
                    if (manager != null) {
                        manager.removeListener(listener);
                    }
                }
            }
        }, false);
    }

    @Override
    public void removeListener(final String path, final ChildrenDataListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                synchronized (childrenDataManagers) {
                    ChildrenDataManager manager = childrenDataManagers.get(path);
                    if (manager != null) {
                        manager.removeListener(listener);
                    }
                }
            }
        }, false);
    }

    @Override
    public void removeListener(final String path, final LeaderListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                synchronized (leaderManagers) {
                    ElectionManager manager = leaderManagers.get(path);
                    if (manager != null) {
                        manager.removeListener(listener);
                    }
                }
            }
        }, false);
    }

    @Override
    public void removeListener(final String path, final ClusterListener listener) {
        if (path == null || path.isEmpty() || listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                synchronized (leaderManagers) {
                    ElectionManager manager = leaderManagers.get(path);
                    if (manager != null) {
                        manager.removeListener(listener);
                    }
                }
            }
        }, false);
    }

    @Override
    public void removeListener(final ConnectionListener listener) {
        if (listener == null) {
            return;
        }
        execute(new Runnable() {
            @Override
            public void run() {
                zkClient.removeListener(listener);
            }
        }, false);
    }

    /**
     * 检查状态
     */
    protected void checkState() {
        if (!isStarted()) {
            throw new IllegalStateException("ZKRegistry has not been started yet!");
        }
    }

    /**
     * 执行
     *
     * @param callable 调用接口
     * @param <T>      结果类型
     * @return 结果
     * @throws RegistryException
     */
    protected <T> T execute(final Callable<T> callable) throws RegistryException {
        return execute(callable, true);
    }

    /**
     * 执行
     *
     * @param callable   调用接口
     * @param checkState 是否检查状态
     * @param <T>
     * @return 结果
     * @throws RegistryException
     */
    protected <T> T execute(final Callable<T> callable, final boolean checkState) throws RegistryException {
        readLock.lock();
        try {
            if (checkState) {
                checkState();
            }
            return callable.call();
        } catch (RegistryException e) {
            throw e;
        } catch (KeeperException.ConnectionLossException e) {
            throw new RegistryException.ConnectionLossException();
        } catch (KeeperException.BadVersionException e) {
            throw new RegistryException.BadVersionException();
        } catch (KeeperException.SessionExpiredException e) {
            throw new RegistryException.SessionExpiredException();
        } catch (KeeperException.OperationTimeoutException e) {
            throw new RegistryException.OperationTimeoutException();
        } catch (KeeperException.DataInconsistencyException e) {
            throw new RegistryException.DataInconsistencyException();
        } catch (KeeperException.NoNodeException e) {
            throw new RegistryException.NoNodeException();
        } catch (KeeperException.NodeExistsException e) {
            throw new RegistryException.NodeExistsException();
        } catch (KeeperException.NotEmptyException e) {
            throw new RegistryException.NotEmptyException();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(e.getMessage(), e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 执行
     *
     * @param runnable   调用接口
     * @param checkState 是否检查状态
     */
    protected void execute(final Runnable runnable, final boolean checkState) {
        readLock.lock();
        try {
            if (checkState) {
                checkState();
            }
            runnable.run();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 启动监听管理器
     *
     * @param manager 监听管理器
     */
    protected void start(final ListenerManager<?, ?> manager) {
        if (manager == null) {
            return;
        }
        try {
            manager.start();
        } catch (Exception ignored) {
        }
    }

    /**
     * 启动监听管理器
     *
     * @param managers 监听管理器
     */
    protected void start(final Map<String, ? extends ListenerManager> managers) {
        if (managers == null) {
            return;
        }
        for (Map.Entry<String, ? extends ListenerManager> entry : managers.entrySet()) {
            start(entry.getValue());
        }
    }
}