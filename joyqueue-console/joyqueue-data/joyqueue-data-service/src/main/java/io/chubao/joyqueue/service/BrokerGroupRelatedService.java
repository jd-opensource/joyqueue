package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.BrokerGroupRelated;
import io.chubao.joyqueue.model.query.QBrokerGroupRelated;

/**
 * 分组服务
 * Created by chenyanying3 on 2018-10-18.
 */
public interface BrokerGroupRelatedService extends PageService<BrokerGroupRelated, QBrokerGroupRelated> {
    int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated);
    int deleteByGroupId(long groupId);
}
