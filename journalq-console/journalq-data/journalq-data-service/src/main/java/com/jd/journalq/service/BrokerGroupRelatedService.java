package com.jd.journalq.service;

import com.jd.journalq.model.domain.BrokerGroupRelated;
import com.jd.journalq.model.query.QBrokerGroupRelated;

/**
 * 分组服务
 * Created by chenyanying3 on 2018-10-18.
 */
public interface BrokerGroupRelatedService extends PageService<BrokerGroupRelated, QBrokerGroupRelated> {
    int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated);
}
