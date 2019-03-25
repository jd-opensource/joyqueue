package com.jd.journalq.service;

import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.monitor.Client;
import com.jd.journalq.model.domain.BrokerTopicMonitor;
import com.jd.journalq.model.query.QMonitor;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public interface BrokerTopicMonitorService {
    PageResult<BrokerTopicMonitor> queryTopicsPartitionMointor(QPageQuery<QMonitor> qPageQuery);
    PageResult<Client> queryClientConnectionDetail(QPageQuery<QMonitor> qPageQuery);
    PageResult<BrokerTopicMonitor> queryTopicsMointor(QPageQuery<QMonitor> qPageQuery);
}
