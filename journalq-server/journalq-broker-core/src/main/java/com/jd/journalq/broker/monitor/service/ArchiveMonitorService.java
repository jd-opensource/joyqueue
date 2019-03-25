package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.common.monitor.ArchiveMonitorInfo;

/**
 * Created by chengzhiliang on 2018/12/18.
 */
public interface ArchiveMonitorService {

    long getConsumeBacklogNum();

    long getSendBackLogNum();

    ArchiveMonitorInfo getArchiveMonitorInfo();

}
