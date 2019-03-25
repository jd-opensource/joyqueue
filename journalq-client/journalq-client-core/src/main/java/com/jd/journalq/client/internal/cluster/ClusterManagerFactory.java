package com.jd.journalq.client.internal.cluster;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.helper.NameServerHelper;
import com.jd.journalq.client.internal.transport.config.TransportConfig;

/**
 * ClusterClientManagerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class ClusterManagerFactory {

    public static ClusterManager create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static ClusterManager create(String address, String app, String token, String region, String namespace) {
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(nameServerConfig);
    }

    public static ClusterManager create(NameServerConfig nameServerConfig) {
        return create(nameServerConfig, new TransportConfig());
    }

    public static ClusterManager create(NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        ClusterClientManager clusterClientManager = ClusterClientManagerFactory.create(nameServerConfig, transportConfig);
        return new ClusterManagerWrapper(nameServerConfig, clusterClientManager);
    }

    public static ClusterManager create(NameServerConfig nameServerConfig, ClusterClientManager clusterClientManager) {
        return new ClusterManager(nameServerConfig, clusterClientManager);
    }
}