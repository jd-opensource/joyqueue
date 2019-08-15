package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.monitor.ArchiveMonitorInfo;

/**
 * Created by chengzhiliang on 2018/12/18.
 */
public interface ArchiveMonitorService {

    /**
     * 获取消费归档数量
     *
     * @return
     */
    long getConsumeBacklogNum();

    /**
     * 获取发送归档数量
     *
     * @return
     */
    long getSendBackLogNum();

    /**
     * 获取归档监控
     *
     * @return
     */
    ArchiveMonitorInfo getArchiveMonitorInfo();

}
