package org.joyqueue.broker.kafka.network.helper;

import org.joyqueue.network.transport.Transport;

/**
 * KafkaSessionHelper
 * author: gaohaoxiang
 * date: 2020/4/10
 */
public class KafkaSessionHelper {

    private static final String IS_AUTH = "_IS_AUTH_";

    public static void setIsAuth(Transport transport, boolean isAuth) {
        transport.attr().set(IS_AUTH, isAuth);
    }

    public static boolean isAuth(Transport transport) {
        Boolean isAuth = transport.attr().get(IS_AUTH);
        if (isAuth == null) {
            return false;
        }
        return isAuth;
    }
}