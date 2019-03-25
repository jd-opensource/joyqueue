package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.BrokerGroup;
import com.jd.journalq.model.domain.BrokerGroupRelated;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.query.QBrokerGroup;
import com.jd.journalq.repository.BrokerGroupRepository;
import com.jd.journalq.service.BrokerGroupRelatedService;
import com.jd.journalq.service.BrokerGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分组服务实现
 * Created by chenyanying3 on 2018-10-18
 */
@Service("brokerGroupService")
public class BrokerGroupServiceImpl extends PageServiceSupport<BrokerGroup, QBrokerGroup, BrokerGroupRepository> implements BrokerGroupService {
    @Autowired
    private BrokerGroupRelatedService brokerGroupRelatedService;

    @Override
    public List<BrokerGroup> findAll(QBrokerGroup qBrokerGroup) {
        return repository.findAll(qBrokerGroup);
    }

    @Override
    public int update(BrokerGroup model) {
        //修改分组同时修改分组关联
        if (model.getId() > 0 && model.getCode() != null) {
            BrokerGroupRelated brokerGroupRelated = new BrokerGroupRelated();
            brokerGroupRelated.setGroup(new Identity(model.getId(), model.getCode()));
            brokerGroupRelatedService.updateGroupByGroupId(brokerGroupRelated);
        }
        return super.update(model);
    }
}
