package com.jd.journalq.client.internal.nameserver.helper;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.domain.TopicName;
import org.apache.commons.lang3.StringUtils;

/**
 * NameServerHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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