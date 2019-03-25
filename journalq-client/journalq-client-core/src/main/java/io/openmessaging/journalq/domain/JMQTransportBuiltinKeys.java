package io.openmessaging.journalq.domain;

import io.openmessaging.OMSBuiltinKeys;

/**
 * JMQTransportBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JMQTransportBuiltinKeys extends OMSBuiltinKeys {

    String CONNECTIONS = "TRANSPORT_CONNECTIONS";

    String SEND_TIMEOUT = "TRANSPORT_SEND_TIMEOUT";

    String IO_THREADS = "TRANSPORT_IO_THREADS";

    String CALLBACK_THREADS = "TRANSPORT_CALLBACK_THREADS";

    String CHANNEL_MAX_IDLE_TIME = "TRANSPORT_CHANNEL_MAX_IDLE_TIME";

    String HEARTBEAT_INTERVAL = "TRANSPORT_HEARTBEAT_INTERVAL";

    String HEARTBEAT_TIMEOUT = "TRANSPORT_HEARTBEAT_TIMEOUT";

    String SO_LINGER = "TRANSPORT_SO_LINGER";

    String TCP_NO_DELAY = "TRANSPORT_TCP_NO_DELAY";

    String KEEPALIVE = "TRANSPORT_KEEPALIVE";

    String SO_TIMEOUT = "TRANSPORT_SO_TIMEOUT";

    String SOCKET_BUFFER_SIZE = "TRANSPORT_SOCKET_BUFFER_SIZE";

    String MAX_ONEWAY = "TRANSPORT_MAX_ONEWAY";

    String MAX_ASYNC = "TRANSPORT_MAX_ASYNC";

    String NONBLOCK_ONEWAY = "TRANSPORT_NONBLOCK_ONEWAY";

    String RETRIES = "TRANSPORT_RETRIES";

}