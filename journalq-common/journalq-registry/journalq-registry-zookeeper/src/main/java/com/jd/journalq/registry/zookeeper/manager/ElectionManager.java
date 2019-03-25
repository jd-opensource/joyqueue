package com.jd.journalq.registry.zookeeper.manager;

import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.registry.util.Path;
import com.jd.journalq.registry.zookeeper.ZKClient;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.os.Systems;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象的Leader监听管理器
 */
public abstract class ElectionManager<L extends EventListener<E>, E> extends ListenerManager<L, E> {

    private static final Logger logger = LoggerFactory.getLogger(ElectionManager.class);
    // 是否是领导节点
    protected AtomicBoolean leader = new AtomicBoolean(false);
    // 注册的节点名称
    protected AtomicReference<String> node = new AtomicReference<String>();
    // 是否是观察者
    protected boolean observer;
    // 身份标示，存放到节点数据里面
    protected String identity;

    public ElectionManager(ZKClient zkClient, String path) {
        this(zkClient, path, null);
    }

    public ElectionManager(ZKClient zkClient, String path, String identity) {
        super(zkClient, path);
        this.identity = identity;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (identity == null || identity.isEmpty()) {
            // 默认身份是"IP:进程ID"
            identity = IpUtil.getLocalIp() + ":" + Systems.getPid();
        }
    }

    @Override
    protected void doStop() {
        try {
            //删除掉Leader节点
            deleteElectionNode();
        } catch (Throwable ignored) {
        }
        // 广播Lost事件
        onLostEvent();
        // 强制复位
        node.set(null);
        leader.set(false);
        super.doStop();
    }

    @Override
    protected void onReconnectedEvent() {
        onConnectedEvent();
    }

    @Override
    protected void onSuspendedEvent() {
        onLostEvent();
    }

    @Override
    protected void onUpdateEvent() throws Exception {
        // first time create ephemeral node
        writeLock.lock();
        try {
            if (!isStarted()) {
                return;
            }
            updateElectionNode();
            // 进行选举
            elect();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 选举操作
     *
     * @throws Exception
     */
    protected abstract void elect() throws Exception;

    @Override
    protected void onRemoveListener(L listener) {
        if (node.get() != null) {
            updateEvents.add(UpdateType.UPDATE);
        }
    }

    /**
     * 是否是Leader
     *
     * @return 是否是Leader
     */
    public boolean isLeader() {
        readLock.lock();
        try {
            return isStarted() && leader.get();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 更新选举节点
     *
     * @throws Exception
     */
    protected void updateElectionNode() throws Exception {
        // 没有业务逻辑关注，所以不需要创建临时节点
        if (events.getListeners().isEmpty()) {
            // 如果原来已经注册过了则删除掉
            if (node.get() != null) {
                leader.set(false);
                deleteElectionNode();
            }
        } else if (!observer) {
            // 不是观察者模式，参与投票，判断是否创建了选举节点
            if (node.get() == null) {
                // 创建Leader选举的临时节点
                createElectionNode();
            } else {
                // 获取当前节点信息
                Stat stat = new Stat();
                String leaderPath = Path.concat(this.path, node.get());
                zkClient.getData(leaderPath, stat);
                if (stat.getCtime() == 0) {
                    // 不存在,创建Leader选举的临时节点
                    createElectionNode();
                } else if (stat.getEphemeralOwner() != zkClient.getSessionId()) {
                    // Session变化了
                    zkClient.delete(leaderPath);
                    createElectionNode();
                }
            }
        }
    }

    /**
     * 删除选举节点
     *
     * @throws Exception
     */
    protected void deleteElectionNode() throws Exception {
        if (node.get() == null) {
            return;
        }
        String child = Path.concat(path, node.get());
        if (zkClient.exists(child)) {
            zkClient.delete(child);
        }
        node.set(null);
        if (logger.isInfoEnabled()) {
            logger.info("delete EPHEMERAL_SEQUENTIAL path" + child);
        }
    }

    /**
     * 创建选举节点
     *
     * @throws Exception
     */
    protected void createElectionNode() throws Exception {
        // 增加IP地址，便于在Zookeeper中查看是那台机器创建的节点
        byte[] data = identity.getBytes(Charsets.UTF_8);
        //创建有序临时节点
        String childFullPath = zkClient.create(Path.concat(path, "member-"), data, CreateMode.EPHEMERAL_SEQUENTIAL);
        //得到节点名称
        String childName = childFullPath.substring(path.length() + 1);
        node.set(childName);
        if (logger.isInfoEnabled()) {
            logger.info("added EPHEMERAL_SEQUENTIAL path" + childFullPath);
        }
    }

}
