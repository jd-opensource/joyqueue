package com.jd.journalq.broker.monitor.service.support;

import com.jd.journalq.broker.archive.ArchiveManager;
import com.jd.journalq.common.monitor.ArchiveMonitorInfo;
import com.jd.journalq.broker.monitor.service.ArchiveMonitorService;

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
