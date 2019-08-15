package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.BrokerGroup;
import io.chubao.joyqueue.model.domain.BrokerGroupRelated;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.query.QBrokerGroup;
import io.chubao.joyqueue.repository.BrokerGroupRepository;
import io.chubao.joyqueue.service.BrokerGroupRelatedService;
import io.chubao.joyqueue.service.BrokerGroupService;
import io.chubao.joyqueue.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Override
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int delete(final BrokerGroup group) {
        brokerGroupRelatedService.deleteByGroupId(group.getId());
        return super.delete(group);
    }
}
