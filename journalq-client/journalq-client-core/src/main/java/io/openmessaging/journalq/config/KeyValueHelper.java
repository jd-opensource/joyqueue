package io.openmessaging.journalq.config;

import io.openmessaging.KeyValue;

/**
 * KeyValueHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/11
 */
public class KeyValueHelper {

    public static int getInt(KeyValue keyValue, String key, int defaultValue) {
        if (!keyValue.containsKey(key)) {
            return defaultValue;
        }
        return keyValue.getInt(key);
    }

    public static String getString(KeyValue keyValue, String key, String defaultValue) {
        if (!keyValue.containsKey(key)) {
            return defaultValue;
        }
        return keyValue.getString(key);
    }
}