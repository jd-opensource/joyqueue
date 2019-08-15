package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.BrokerGroup;
import io.chubao.joyqueue.model.query.QBrokerGroup;

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


    /**
     * 绑定\解绑分组
     * @param model
     */
    void updateBroker(Broker model);
}
