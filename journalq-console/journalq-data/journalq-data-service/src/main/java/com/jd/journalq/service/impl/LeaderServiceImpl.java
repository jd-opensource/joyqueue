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

import com.alibaba.fastjson.JSON;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.convert.CodeConverter;
import com.jd.journalq.exception.ServiceException;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.service.LeaderService;
import com.jd.journalq.nsr.BrokerNameServerService;
import com.jd.journalq.nsr.PartitionGroupServerService;
import com.jd.journalq.nsr.TopicNameServerService;
import com.jd.journalq.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.jd.journalq.exception.ServiceException.INTERNAL_SERVER_ERROR;

@Service("leaderService")
public class LeaderServiceImpl implements LeaderService {
    private Logger logger= LoggerFactory.getLogger(BrokerMonitorServiceImpl.class);

    @Autowired
    private BrokerNameServerService brokerNameServerService;

    @Autowired
    private TopicNameServerService topicNameServerService;

    @Autowired
    private PartitionGroupServerService partitionGroupServerService;

    @Override
    public List<PartitionGroup> findPartitionGroupLeaderBroker(String topic,String namespace) {
        try {
            List<TopicPartitionGroup> topicPartitionGroups = partitionGroupServerService.findByTopic(topic,namespace);
            return findPartitionGroupLeaderBroker(topicPartitionGroups);
        }catch (Exception e){
            throw new ServiceException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private List<PartitionGroup> findPartitionGroupLeaderBroker(List<TopicPartitionGroup> topicPartitionGroups){
        if(NullUtil.isEmpty(topicPartitionGroups)) throw new IllegalArgumentException("topic partition group is null");
        try {
            return topicNameServerService.findPartitionGroupMaster(topicPartitionGroups);
        }catch (Exception e){
            String errorMsg = String.format("topicId %d master group request error", topicPartitionGroups.get(0).getTopic().getId());
            logger.error(errorMsg, e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, errorMsg);
        }
    }


    @Override
    public Map.Entry<PartitionGroup, Broker> findPartitionGroupLeaderBrokerDetail(String namespace,String topic,int groupNo) {
        List<TopicPartitionGroup> topicPartitionGroups=new ArrayList<>();
        TopicPartitionGroup topicPartitionGroup=partitionGroupServerService.findByTopicAndGroup(namespace,topic,groupNo);
        topicPartitionGroups.add(topicPartitionGroup);
        List<Map.Entry<PartitionGroup, Broker>> partitionGroupLeaderBroker=findPartitionGroupLeaderBrokerDetail(topicPartitionGroups);
        if(!NullUtil.isEmpty(partitionGroupLeaderBroker)&&partitionGroupLeaderBroker.size()>0) {
            return partitionGroupLeaderBroker.get(0);
        }else return null;
    }


    @Override
    public Map.Entry<PartitionGroup, Broker> findPartitionLeaderBrokerDetail(String namespace, String topic, int partition) {
        List<TopicPartitionGroup> topicPartitionGroups=partitionGroupServerService.findByTopic(CodeConverter.convertTopic(new Namespace(namespace),new Topic(topic)).getFullName());
        TopicPartitionGroup tp=null;
        for(TopicPartitionGroup t:topicPartitionGroups){
            Set<Short> partitions=parsePartitions(t);
            if(partitions.contains((short)partition)){
                tp=t;
                break;
            }
        }
        return tp==null?null:findPartitionGroupLeaderBrokerDetail(namespace,topic,tp.getGroupNo());
    }

    @Override
    public List<Broker> findLeaderBroker(String topic,String namespace) {
        List<PartitionGroup> partitionGroups = findPartitionGroupLeaderBroker(topic,namespace);
        if (NullUtil.isEmpty(partitionGroups)) {
            return null;
        }
        /**
         *
         * deal local single broker test,单机部署 无主
         **/
        Set<Integer> brokerIds =  partitionGroups.stream().map(
                partitionGroup -> partitionGroup.getLeader()).collect(Collectors.toSet());
//        List<Broker> brokers=brokerRepository.findByIds(brokerIds);
        List<Broker> brokers = null;
        try {
            brokers = brokerNameServerService.getByIdsBroker(new ArrayList<>(brokerIds));
        } catch (Exception e) {
            logger.error("getByIdsBroker error",e);
        }
        if (NullUtil.isEmpty(brokers)) {
            // deal local single broker test
            if(brokers==null) brokers=new ArrayList<>();
        }
        return brokers;
    }

    @Override
    public List<Map.Entry<PartitionGroup, Broker>> findPartitionGroupLeaderBrokerDetail(String topic,String namespace) {
        return findPartitionGroupLeaderBrokerDetail(partitionGroupServerService.findByTopic(topic,namespace));

    }


    @Override
    public Map<Short, Broker> findPartitionLeaderBrokerDetail(String topic, String namespace) {
        Map<Short,Broker> partitionBrokerMap=new HashMap<>();
        List<Map.Entry<PartitionGroup, Broker>> partitionGroupBrokers=findPartitionGroupLeaderBrokerDetail(partitionGroupServerService.findByTopic(topic,namespace));
        for(Map.Entry<PartitionGroup, Broker> e:partitionGroupBrokers){
           Set<Short> partitions=e.getKey().getPartitions();
           for(Short p:partitions) {
               partitionBrokerMap.put(p,e.getValue());
           }
        }
        return partitionBrokerMap;
    }

    /**
     * @param topicPartitionGroups
     * @return  a pair of partitionGroup,Broker
     **/
  private   List<Map.Entry<PartitionGroup, Broker>> findPartitionGroupLeaderBrokerDetail(List<TopicPartitionGroup> topicPartitionGroups){

      List<Map.Entry<PartitionGroup, Broker>>  partitionGroupBroker=new ArrayList<>();
      List<PartitionGroup> partitionGroups = findPartitionGroupLeaderBroker(topicPartitionGroups);
      if (NullUtil.isEmpty(partitionGroups)) {
          return partitionGroupBroker;
      }
      /**
       *
       * deal local single broker test,单机部署 无主
       **/
      List<Integer> brokerIds =  partitionGroups.stream().map(
              partitionGroup -> partitionGroup.getLeader()).collect(Collectors.toList());
      List<Broker> brokers= null;
      try {
          //去重broker id
          List<Integer> brokerIdList =  brokerIds.stream().distinct().collect(Collectors.toList());
          brokers = brokerNameServerService.getByIdsBroker(brokerIdList);
      } catch (Exception e) {
          logger.error("brokerNameServerService.getByIdsBroker error",e);
      }
      // deal local single broker test
      if(NullUtil.isEmpty(brokers)) return partitionGroupBroker;
      Broker broker;
      Map<Long,Broker>  brokerMap= brokers.stream().collect(Collectors.toMap(Broker::getId,b -> b));
      for(PartitionGroup pg:partitionGroups){
          broker=brokerMap.get(Long.valueOf(pg.getLeader()));
          if(broker==null){
              logger.info("broker not found ");
          }
          partitionGroupBroker.add(new HashMap.SimpleEntry(pg,broker));
      }
      return partitionGroupBroker;
  }

    public Set<Short> parsePartitions(TopicPartitionGroup partitionGroup){
        Set<Short> partitionSet=new HashSet<>();
        String partitions=partitionGroup.getPartitions();
        if(partitions!=null&&partitions.trim().length()!=0){
            List<String> partitionStrs= JSON.parseArray(partitions,String.class);
            for(String p:partitionStrs){
                if(p.trim().length()!=0){
                    partitionSet.add(Short.valueOf(p));
                }
            }
        }
        return partitionSet;
    }

}
