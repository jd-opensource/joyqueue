package com.jd.journalq.service;

import com.jd.journalq.model.domain.BrokerGroup;
import com.jd.journalq.model.query.QBrokerGroup;

import java.util.List;

/**
 * 分组服务
 * Created by chenyanying3 on 2018-10-18.
 */
public interface BrokerGroupService extends PageService<BrokerGroup, QBrokerGroup> {
    /**
     * 查找全部
     * @return
     */
    List<BrokerGroup> findAll(QBrokerGroup qBrokerGroup);
}
