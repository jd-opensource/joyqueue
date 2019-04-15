/**
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
package com.jd.journalq.service.impl;

import com.jd.journalq.model.ListQuery;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QBroker;
import com.jd.journalq.model.query.QBrokerGroupRelated;
import com.jd.journalq.model.query.QPartitionGroupReplica;
import com.jd.journalq.nsr.BrokerNameServerService;
import com.jd.journalq.service.BrokerGroupRelatedService;
import com.jd.journalq.service.BrokerService;
import com.jd.journalq.service.PartitionGroupReplicaService;
import com.jd.journalq.util.LocalSession;
import com.jd.journalq.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    public Broker findById(Long id) throws Exception {
        return brokerNameServerService.findById(id);
    }

    @Override
    public List<Broker> findByTopic(String topic) throws Exception {
        QPartitionGroupReplica qReplica = new QPartitionGroupReplica();
        qReplica.setTopic(new Topic(topic));
        List<PartitionGroupReplica> replicas = partitionGroupReplicaService.findByQuery(qReplica);
        if (NullUtil.isEmpty(replicas)) {
            logger.error(String.format("can not find partitionGroup by topic, topic is %s", topic));
            return null;
        }
        return replicas.stream().map(replica -> {
                try {
                    return this.findById(new Long(replica.getBrokerId()));
                } catch (Exception e) {
                    logger.error(String.format("can not find broker with id %s"), replica.getBroker().getId());
                    return null;
                }
            }).collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                    new TreeSet<>(comparing(Broker::getId))), ArrayList::new));
    }

    @Override
    public PageResult<Broker> findByQuery(QPageQuery<QBroker> query) {
        try {
            if(query != null && query.getQuery() != null) {
                List<Long> brokerIds = getBrokerIdByGroupIds(query.getQuery());
                if (brokerIds == null) {
                    return PageResult.empty();
                }
                query.getQuery().setInBrokerIds(brokerIds);
            }
            PageResult<Broker> pageResult = brokerNameServerService.findByQuery(query);
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
        } catch (Exception e) {
            logger.error("findByQuery error",e);
            throw new RuntimeException("findByQuery error",e);
        }
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
    public List<Broker> findByQuery(QBroker query) throws Exception {
        if(query != null) {
            query.setInBrokerIds(getBrokerIdByGroupIds(query));
        }
        return brokerNameServerService.findByQuery(query);
    }

    @Override
    public List<Broker> queryBrokerList(QBroker qBroker) throws Exception {
        ListQuery<QBrokerGroupRelated> brokerListQuery = new ListQuery<>();
        QBrokerGroupRelated qBrokerGroupRelated = new QBrokerGroupRelated();
        qBrokerGroupRelated.setGroup(qBroker.getGroup());
        brokerListQuery.setQuery(qBrokerGroupRelated);
        List<BrokerGroupRelated> brokerList = brokerGroupRelatedService.findByQuery(brokerListQuery);
        if (brokerList != null && brokerList.size() > 0) {
            List<Long> brokerIdList = brokerList.stream().map(broker -> broker.getId()).collect(Collectors.toList());
            qBroker.setInBrokerIds(brokerIdList);
        } else {
            if (qBrokerGroupRelated.getGroup() != null) {
                return new ArrayList<>();
            }
        }
        return brokerNameServerService.findByQuery(qBroker);
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
}
