/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.kafka.helper;

import org.apache.commons.lang3.StringUtils;

/**
 * KafkaClientHelper
 *
 * author: gaohaoxiang
 * date: 2018/11/12
 */
public class KafkaClientHelper {

    private static final String SEPARATOR = "-";
    private static final String AUTH_SEPARATOR = "@";
    private static final String[] REPLACE = {"spark-executor-"};
    private static final String[] METADATA_FUZZY_SEARCH = {"MetadataFuzzySearch"};

    public static String parseClient(String clientId) {
        clientId = doParseClient(clientId);
        if (StringUtils.contains(clientId, AUTH_SEPARATOR)) {
            String[] strings = StringUtils.splitByWholeSeparator(clientId, AUTH_SEPARATOR);
            clientId = strings[0];
        }
        return clientId;
    }

    public static boolean isMetadataFuzzySearch(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return false;
        }
        String[] strings = StringUtils.splitByWholeSeparator(clientId, SEPARATOR);
        for (String fuzzySearch : METADATA_FUZZY_SEARCH) {
            if (strings[strings.length - 1].equals(fuzzySearch)) {
                return true;
            }
        }
        return false;
    }

    public static String parseToken(String clientId) {
        clientId = doParseClient(clientId);
        if (StringUtils.contains(clientId, AUTH_SEPARATOR)) {
            String[] strings = StringUtils.splitByWholeSeparator(clientId, AUTH_SEPARATOR);
            return strings[1];
        }
        return null;
    }

    protected static String doParseClient(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return clientId;
        }
        for (String replace : REPLACE) {
            clientId = StringUtils.replace(clientId, replace, "");
        }
        if (StringUtils.contains(clientId, SEPARATOR)) {
            String[] strings = StringUtils.splitByWholeSeparator(clientId, SEPARATOR);
            clientId = strings[0];
        }
        return clientId;
    }
}