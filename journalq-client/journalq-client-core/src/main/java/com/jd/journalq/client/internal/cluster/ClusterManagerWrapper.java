package com.jd.journalq.client.internal.cluster;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;

/**
 * ClusterManagerWrapper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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