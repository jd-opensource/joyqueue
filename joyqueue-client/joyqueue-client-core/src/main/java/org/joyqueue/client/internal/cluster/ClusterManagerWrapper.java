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
package org.joyqueue.client.internal.cluster;

import org.joyqueue.client.internal.nameserver.NameServerConfig;

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