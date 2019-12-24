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
package org.joyqueue.client.internal.consumer.transport;

import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * ConsumerClientManagerFactory
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class ConsumerClientManagerFactory {

    public static ConsumerClientManager create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static ConsumerClientManager create(String address, String app, String token, String region, String namespace) {
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(nameServerConfig, new TransportConfig());
    }

    public static ConsumerClientManager create(NameServerConfig nameServerConfig) {
        return create(nameServerConfig, new TransportConfig());
    }

    public static ConsumerClientManager create(NameServerConfig nameServerConfig, TransportConfig transportConfig) {
        return new ConsumerClientManager(transportConfig, nameServerConfig);
    }
}