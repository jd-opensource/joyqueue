package com.jd.journalq.service;


import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.model.domain.Broker;

import java.util.List;
import java.util.Map;

/**
 *
 *  提供topic 选主信息
 *
 **/
public interface LeaderService {

    /**
     *  根据topic id 查找,topic partition group 的主broker
     *  @return  partitionGroup list ,contain broker id
     *  or null, if not found
     **/
    List<PartitionGroup> findPartitionGroupLeaderBroker(String topic,String namespace);

    /***
     *
     *  根据topic id 查找,topic partition group 的主broker
     *  @param topic
     *  @return  Broker list or null ,if not found
     **/
    List<Broker> findLeaderBroker(String topic,String namespace);


    /***
     *
     *  根据topic id 查找,topic partition group 的主broker
     *  @param namespace  topic namespace id
     *  @param topic
     *  @param groupNo
     *  @return  a pair of partitionGroup,Broker list or null ,if not found
     **/
    Map.Entry<PartitionGroup, Broker> findPartitionGroupLeaderBrokerDetail(String namespace,String topic,int groupNo);


    /***
     *
     *  根据topic id 查找,topic partition 的主broker
     *  @param topic
     *  @param partition
     *  @return  a pair of partitionGroup,Broker list or null ,if not found
     **/
    Map.Entry<PartitionGroup, Broker> findPartitionLeaderBrokerDetail(String namespace,String topic,int partition);

    /**
     * @return  <PartitionGroup,Broker> kv list,parition group with leader broker detail
     *
     **/
    List<Map.Entry<PartitionGroup,Broker>> findPartitionGroupLeaderBrokerDetail(String topic,String namespace);





}
