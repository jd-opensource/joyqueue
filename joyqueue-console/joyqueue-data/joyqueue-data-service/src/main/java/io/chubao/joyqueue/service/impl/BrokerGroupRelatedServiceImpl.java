package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.BrokerGroupRelated;
import io.chubao.joyqueue.model.query.QBrokerGroupRelated;
import io.chubao.joyqueue.repository.BrokerGroupRelatedRepository;
import io.chubao.joyqueue.service.BrokerGroupRelatedService;
import org.springframework.stereotype.Service;

/**
 * 分组服务实现
 * Created by chenyanying3 on 2018-10-18
 */
@Service("brokerGroupRelatedService")
public class BrokerGroupRelatedServiceImpl extends PageServiceSupport<BrokerGroupRelated, QBrokerGroupRelated, BrokerGroupRelatedRepository> implements BrokerGroupRelatedService {

    @Override
    public int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated) {
        return repository.updateGroupByGroupId(brokerGroupRelated);
    }

    @Override
    public int deleteByGroupId(long groupId) {
        return repository.deleteByGroupId(groupId);
    }
}
