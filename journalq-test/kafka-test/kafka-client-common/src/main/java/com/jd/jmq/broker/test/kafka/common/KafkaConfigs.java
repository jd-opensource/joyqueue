package com.jd.journalq.broker.test.kafka.common;


import com.jd.journalq.toolkit.network.IpUtil;

/**
 * Created by zhuduohui on 2018/12/17.
 */
public interface KafkaConfigs {

    String GROUP_ID = "group1";
    String TOPIC = "default.topic1";
    String BOOTSTRAP = IpUtil.getLocalIp() + ":50088";
    String PRODUCE_CLIENT_ID = "group1";
    String CONSUME_CLIENT_ID = "group1";

}