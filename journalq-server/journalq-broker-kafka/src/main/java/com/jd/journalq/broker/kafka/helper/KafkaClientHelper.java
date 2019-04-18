package com.jd.journalq.broker.kafka.helper;

import org.apache.commons.lang3.StringUtils;

/**
 * KafkaClientHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public class KafkaClientHelper {

    private static final String SEPARATOR = "-";
    private static final String[] REPLACE = {"spark-executor-"};

    public static String parseClient(String clientId) {
        for (String replace : REPLACE) {
            clientId = clientId.replace(replace, "");
        }
        if (StringUtils.contains(clientId, SEPARATOR)) {
            String[] strings = StringUtils.splitByWholeSeparator(clientId, SEPARATOR);
//            if (StringUtils.isNumeric(strings[strings.length - 1])) {
//                return clientId.substring(0, clientId.lastIndexOf(SEPARATOR));
//            }
            return strings[0];
        }
        return clientId;
    }
}