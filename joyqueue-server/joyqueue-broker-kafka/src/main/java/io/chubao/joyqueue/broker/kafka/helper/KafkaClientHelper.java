package io.chubao.joyqueue.broker.kafka.helper;

import org.apache.commons.lang3.StringUtils;

/**
 * KafkaClientHelper
 *
 * author: gaohaoxiang
 * date: 2018/11/12
 */
public class KafkaClientHelper {

    private static final String SEPARATOR = "-";
    private static final String[] REPLACE = {"spark-executor-"};

    public static String parseClient(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return clientId;
        }
        for (String replace : REPLACE) {
            clientId = StringUtils.replace(clientId, replace, "");
        }
        if (StringUtils.contains(clientId, SEPARATOR)) {
            String[] strings = StringUtils.splitByWholeSeparator(clientId, SEPARATOR);
            return strings[0];
        }
        return clientId;
    }
}