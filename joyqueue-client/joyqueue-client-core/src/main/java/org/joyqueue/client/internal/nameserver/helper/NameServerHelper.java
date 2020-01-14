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
package org.joyqueue.client.internal.nameserver.helper;

import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.domain.TopicName;
import org.apache.commons.lang3.StringUtils;

/**
 * NameServerHelper
 *
 * author: gaohaoxiang
 * date: 2018/12/20
 */
public class NameServerHelper {

    public static NameServerConfig createConfig(String address, String app, String token) {
        return createConfig(address, app, token, null, null);
    }

    public static NameServerConfig createConfig(String address, String app, String token, String region, String namespace) {
        NameServerConfig nameServerConfig = new NameServerConfig();
        nameServerConfig.setAddress(address);
        nameServerConfig.setApp(app);
        nameServerConfig.setToken(token);
        nameServerConfig.setRegion(region);
        nameServerConfig.setNamespace(namespace);
        return nameServerConfig;
    }

    public static String getTopicFullName(String topic, NameServerConfig config) {
        // 如果写了namespace, 那么按照传入的namespace，否则拼上nameserver的namespace
        TopicName topicName = TopicName.parse(topic);
        if (StringUtils.isNotBlank(topicName.getNamespace())) {
            return topicName.getFullName();
        }
        return TopicName.parse(topic, config.getNamespace()).getFullName();
    }
}