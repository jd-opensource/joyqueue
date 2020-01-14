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
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * ClusterClientManagerFactory
 *
 * author: gaohaoxiang
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