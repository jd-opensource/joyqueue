package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.BrokerGroupRelated;
import com.jd.journalq.model.query.QBrokerGroupRelated;
import com.jd.journalq.repository.BrokerGroupRelatedRepository;
import com.jd.journalq.service.BrokerGroupRelatedService;
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
}
