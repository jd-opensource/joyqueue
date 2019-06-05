/**
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