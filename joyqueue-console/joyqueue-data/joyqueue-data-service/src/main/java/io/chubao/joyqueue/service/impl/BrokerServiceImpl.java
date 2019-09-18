/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.BrokerGroupRelated;
import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.model.query.QBroker;
import io.chubao.joyqueue.model.query.QBrokerGroupRelated;
import io.chubao.joyqueue.nsr.BrokerNameServerService;
import io.chubao.joyqueue.service.BrokerGroupRelatedService;
import io.chubao.joyqueue.service.BrokerService;
import io.chubao.joyqueue.service.PartitionGroupReplicaService;
import io.chubao.joyqueue.util.NullUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;


/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
@Service("brokerService")
public class BrokerServiceImpl implements BrokerService {
    private final Logger logger = LoggerFactory.getLogger(BrokerServiceImpl.class);

    @Autowired
    protected BrokerNameServerService brokerNameServerService;
    @Autowired
    private BrokerGroupRelatedService brokerGroupRelatedService;
    @Autowired
    private PartitionGroupReplicaService partitionGroupReplicaService;

    @Override
    public Broker findById(Integer id) throws Exception {
        return brokerNameServerService.findById(id);
    }

    @Override
    public List<Broker> findByTopic(String topic) throws Exception {
        TopicName topicName = TopicName.parse(topic);
        List<PartitionGroupReplica> replicas = partitionGroupReplicaService.getByTopic(topicName.getCode(), topicName.getNamespace());
        if (NullUtil.isEmpty(replicas)) {
            logger.error(String.format("can not find partitionGroup by topic, topic is %s", topic));
            return null;
        }
        return replicas.stream().map(replica -> {
                try {
                    return this.findById(replica.getBrokerId());
                } catch (Exception e) {
                    logger.error(String.format("can not find broker with id %s"), replica.getBrokerId());
                    return null;
                }
            }).collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                    new TreeSet<>(comparing(Broker::getId))), ArrayList::new));
    }

    @Override
    public List<Broker> findByGroup(long group) throws Exception {
        QBrokerGroupRelated qBrokerGroupRelated = new QBrokerGroupRelated();
        qBrokerGroupRelated.setBrokerGroupId(group);
        List<BrokerGroupRelated> brokerGroupRelateds = brokerGroupRelatedService.findByQuery(new ListQuery<>(qBrokerGroupRelated));

        List<Integer> brokerIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(brokerGroupRelateds)) {
            for (BrokerGroupRelated brokerGroupRelated : brokerGroupRelateds) {
                brokerIds.add(Integer.valueOf(String.valueOf(brokerGroupRelated.getId())));
            }
        }

        return getByIdsBroker(brokerIds);
    }

    @Override
    public int add(Broker model) {
        try {
            return brokerNameServerService.add(model);
        } catch (Exception e) {
            logger.error("add error",e);
        }
        return 0;
    }

    @Override
    public int delete(Broker model) {
        try {
            return brokerNameServerService.delete(model);
        } catch (Exception e) {
            logger.error("delete error",e);
        }
        return 0;
    }

    @Override
    public int update(Broker model) {
        try {
            return brokerNameServerService.update(model);
        } catch (Exception e) {
            logger.error("update error",e);
        }
        return 0;
    }

    @Override
    public List<Broker> queryBrokerList(QBroker qBroker) throws Exception {
        ListQuery<QBrokerGroupRelated> brokerListQuery = new ListQuery<>();
        QBrokerGroupRelated qBrokerGroupRelated = new QBrokerGroupRelated();
        qBrokerGroupRelated.setGroup(qBroker.getGroup());
        brokerListQuery.setQuery(qBrokerGroupRelated);
        List<BrokerGroupRelated> brokerList = brokerGroupRelatedService.findByQuery(brokerListQuery);
        if (brokerList != null && brokerList.size() > 0) {
            List<Integer> brokerIdList = brokerList.stream().map(broker -> Integer.valueOf(String.valueOf(broker.getId()))).collect(Collectors.toList());
            qBroker.setInBrokerIds(brokerIdList);
        } else {
            if (qBrokerGroupRelated.getGroup() != null) {
                return new ArrayList<>();
            }
        }
        return brokerNameServerService.getByIdsBroker(qBroker.getInBrokerIds());
    }

    /**
     * 通过组获取brokerIds
     * @param query
     * @return
     */
    private List<Long> getBrokerIdByGroupIds(QBroker query) {
        if (query.getBrokerGroupId() > 0) {
            Integer brokerGroupId = Math.toIntExact(query.getBrokerGroupId());
            if (query.getBrokerGroupIds() != null) {
                query.getBrokerGroupIds().add(brokerGroupId);
            } else {
                query.setBrokerGroupIds(Arrays.asList(brokerGroupId));
            }
        }
        if(query.getBrokerGroupIds() != null && query.getBrokerGroupIds().size() > 0) {
            ListQuery<QBrokerGroupRelated> brokerListQuery = new ListQuery<>();
            QBrokerGroupRelated qBrokerGroupRelated = new QBrokerGroupRelated();
            //数据库查询
            qBrokerGroupRelated.setBrokerGroupIds(query.getBrokerGroupIds());
            qBrokerGroupRelated.setNotInBrokerIds(query.getNotInBrokerIds());
            brokerListQuery.setQuery(qBrokerGroupRelated);
            List<BrokerGroupRelated> brokerList = brokerGroupRelatedService.findByQuery(brokerListQuery);
            if (brokerList != null && brokerList.size() > 0) {
                List<Long> brokerIdList = brokerList.stream().map(broker -> broker.getId()).collect(Collectors.toList());
                return brokerIdList;
            } else {
                //查询到0个 broker 则直接返回
                return null;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Broker> getByIdsBroker(List<Integer> ids) throws Exception {
        return brokerNameServerService.getByIdsBroker(ids);
    }

    @Override
    public List<Broker> syncBrokers() throws Exception {
        return brokerNameServerService.syncBrokers();
    }

    @Override
    public PageResult<Broker> search(QPageQuery<QBroker> qPageQuery) throws Exception {
        // TODO 分组处理
        PageResult<Broker> pageResult = brokerNameServerService.search(qPageQuery);
        if (pageResult !=null && pageResult.getResult() != null && pageResult.getResult().size() >0) {
            List<Broker> brokerList = pageResult.getResult();
            brokerList.stream().map(broker -> {
                BrokerGroupRelated brokerRelated = brokerGroupRelatedService.findById(broker.getId());
                if (brokerRelated != null && brokerRelated.getGroup() != null) {
                    broker.setGroup(brokerRelated.getGroup());
                    broker.setStatus(0);
                }
                return broker;
            }).collect(Collectors.toList());
        }
        return pageResult;
    }
}
