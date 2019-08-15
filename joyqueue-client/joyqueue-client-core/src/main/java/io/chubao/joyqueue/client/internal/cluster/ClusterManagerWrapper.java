package io.chubao.joyqueue.client.internal.cluster;

import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;

/**
 * ClusterManagerWrapper
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class ClusterManagerWrapper extends ClusterManager {

    private ClusterClientManager clusterClientManager;

    public ClusterManagerWrapper(NameServerConfig nameServerConfig, ClusterClientManager clusterClientManager) {
        super(nameServerConfig, clusterClientManager);
        this.clusterClientManager = clusterClientManager;
    }

    @Override
    protected void doStart() throws Exception {
        clusterClientManager.start();
        super.doStart();
    }

    @Override
    protected void doStop() {
        clusterClientManager.stop();
        super.doStop();
    }
}