package io.chubao.joyqueue.client.internal.cluster;

import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * ClusterClientManagerFactory
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class ClusterClientManagerFactory {

    public static ClusterClientManager create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static ClusterClientManager create(String address, String app, String token, String region, String namespace) {
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(nameServerConfig, new TransportConfig());
    }

    public static ClusterClientManager create(NameServerConfig nameServerConfig) {
        return create(nameServerConfig, new TransportConfig());
    }

    public static ClusterClientManager create(NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        return new ClusterClientManager(transportConfig, nameServerConfig);
    }
}