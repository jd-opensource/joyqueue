package com.jd.journalq.broker.kafka.session;

import com.jd.journalq.common.network.transport.Transport;

/**
 * KafkaConnectionHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/9
 */
public class KafkaConnectionHelper {

    private static final String CONNECTION_ATTR_KEY = "_KAFKA_CONNECTION_";

    public static void setConnection(Transport transport, KafkaConnection connection) {
        transport.attr().set(CONNECTION_ATTR_KEY, connection);
    }

    public static KafkaConnection getConnection(Transport transport) {
        return (KafkaConnection) transport.attr().get(CONNECTION_ATTR_KEY);
    }
}