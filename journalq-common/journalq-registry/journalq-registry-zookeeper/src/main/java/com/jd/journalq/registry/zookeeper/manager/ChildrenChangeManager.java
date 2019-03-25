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