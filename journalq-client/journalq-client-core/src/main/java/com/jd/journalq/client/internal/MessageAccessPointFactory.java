package com.jd.journalq.client.internal;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.helper.NameServerHelper;
import com.jd.journalq.client.internal.support.DefaultMessageAccessPoint;
import com.jd.journalq.client.internal.transport.config.TransportConfig;

/**
 * MessageAccessPointFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class MessageAccessPointFactory {

    public static MessageAccessPoint create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessageAccessPoint create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessageAccessPoint create(String address, String app, String token, String region, String namespace) {
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(nameServerConfig, new TransportConfig());
    }

    public static MessageAccessPoint create(NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        return new DefaultMessageAccessPoint(nameServerConfig, transportConfig);
    }

}