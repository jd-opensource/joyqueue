package com.jd.journalq.broker.kafka.conf;

import com.jd.journalq.toolkit.network.IpUtil;

/**
 * Created by zhuduohui on 2018/9/6.
 */
public interface KafkaConfigs {

    final static String GROUP_ID = "test_app";
    final static String TOPIC = "test_topic_0";
    final static int TOPIC_COUNT = 2;
    final static String BOOTSTRAP = IpUtil.getLocalIp() + ":50088";
    final static String CLIENT_ID = "test_app";

//    final static String GROUP_ID = "zhuduohui";
//    final static String TOPIC = "test2";
//    final static int TOPIC_COUNT = 10;
//    final static String BOOTSTRAP = "192.168.112.92:50088";
//    final static String CLIENT_ID = "zhuduohui";
}