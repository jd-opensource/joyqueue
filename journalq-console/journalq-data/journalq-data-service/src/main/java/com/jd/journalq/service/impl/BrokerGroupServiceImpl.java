package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.BrokerGroup;
import com.jd.journalq.model.domain.BrokerGroupRelated;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.query.QBrokerGroup;
import com.jd.journalq.repository.BrokerGroupRepository;
import com.jd.journalq.service.BrokerGroupRelatedService;
import com.jd.journalq.service.BrokerGroupService;
import com.jd.journalq.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 分组服务实现
 * Created by chenyanying3 on 2018-10-18
 */
@Service("brokerGroupService")
public class BrokerGroupServiceImpl extends PageServiceSupport<BrokerGroup, QBrokerGroup, BrokerGroupRepository> implements BrokerGroupService {
    private final Logger logger = LoggerFactory.getLogger(BrokerGroupServiceImpl.class);

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

    /**
     * 增加绑定关系
     * @param model
     */
    public void updateBroker(Broker model){
        if (model != null && model.getGroup() != null) {
            BrokerGroupRelated brokerGroupRelated = new BrokerGroupRelated();
            brokerGroupRelated.setId(model.getId());
            brokerGroupRelated.setGroup(model.getGroup());
            try {
                brokerGroupRelated.setUpdateTime(new Date());
                brokerGroupRelated.setUpdateBy(new Identity(LocalSession.getSession().getUser()));
                BrokerGroupRelated oldBrokerGroupRelated = brokerGroupRelatedService.findById(brokerGroupRelated.getId());
                if (oldBrokerGroupRelated == null) {
                    brokerGroupRelated.setCreateTime(new Date());
                    brokerGroupRelated.setCreateBy(new Identity(LocalSession.getSession().getUser()));
                    brokerGroupRelatedService.add(brokerGroupRelated);
                } else {
                    brokerGroupRelatedService.update(brokerGroupRelated);
                }
            } catch (Exception e) {
                logger.error("绑定异常 error",e);
            }
        }
    }
}
