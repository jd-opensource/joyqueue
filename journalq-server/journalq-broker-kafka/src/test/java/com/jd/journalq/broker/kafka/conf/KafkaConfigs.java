package com.jd.journalq.broker.kafka.conf;

import com.jd.journalq.toolkit.network.IpUtil;

/**
 * Created by zhuduohui on 2018/9/6.
 */
public interface KafkaConfigs {

    static final String GROUP_ID = "test_app";
    static final String TOPIC = "test_topic_0";
    static final int TOPIC_COUNT = 2;
    static final String BOOTSTRAP = IpUtil.getLocalIp() + ":50088";
    static final String CLIENT_ID = "test_app";
    static final String TRANSACTION_ID = "test_transaction";

//    static final String GROUP_ID = "zhuduohui";
//    static final String TOPIC = "test2";
//    static final int TOPIC_COUNT = 10;
//    static final String BOOTSTRAP = "192.168.112.92:50088";
//    static final String CLIENT_ID = "zhuduohui";
}