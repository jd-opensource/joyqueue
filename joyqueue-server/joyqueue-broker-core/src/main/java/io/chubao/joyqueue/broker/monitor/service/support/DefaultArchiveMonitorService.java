package io.chubao.joyqueue.broker.monitor.service.support;

import io.chubao.joyqueue.broker.archive.ArchiveManager;
import io.chubao.joyqueue.monitor.ArchiveMonitorInfo;
import io.chubao.joyqueue.broker.monitor.service.ArchiveMonitorService;

/**
 * Created by chengzhiliang on 2018/12/18.
 */
public class DefaultArchiveMonitorService implements ArchiveMonitorService {

    private ArchiveManager archiveManager;

    public DefaultArchiveMonitorService(ArchiveManager archiveManager) {
        this.archiveManager = archiveManager;
    }

    @Override
    public long getConsumeBacklogNum() {
        return archiveManager.getConsumeBacklogNum();
    }

    @Override
    public long getSendBackLogNum() {
        return archiveManager.getSendBacklogNum();
    }

    @Override
    public ArchiveMonitorInfo getArchiveMonitorInfo() {
        long consumeBacklogNum = getConsumeBacklogNum();
        long sendBackLogNum = getSendBackLogNum();
        ArchiveMonitorInfo info = new ArchiveMonitorInfo();
        info.setConsumeBacklog(consumeBacklogNum);
        info.setConsumeBacklog(sendBackLogNum);

        return info;
    }
}
