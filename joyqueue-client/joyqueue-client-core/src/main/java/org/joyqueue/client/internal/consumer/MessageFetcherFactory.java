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
package org.joyqueue.client.internal.consumer;

import org.joyqueue.client.internal.consumer.config.FetcherConfig;
import org.joyqueue.client.internal.consumer.support.DefaultMessageFetcher;
import org.joyqueue.client.internal.consumer.support.MessageFetcherWrapper;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessageFetcherFactory
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class MessageFetcherFactory {

    public static MessageFetcher create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessageFetcher create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessageFetcher create(String address, String app, String token, String region, String namespace) {
        return create(NameServerHelper.createConfig(address, app, token, region, namespace));
    }

    public static MessageFetcher create(NameServerConfig nameServerConfig) {
        return create(nameServerConfig, new FetcherConfig());
    }

    public static MessageFetcher create(NameServerConfig nameServerConfig, FetcherConfig config) {
        return create(new TransportConfig(), nameServerConfig, config);
    }

    public static MessageFetcher create(TransportConfig transportConfig, NameServerConfig nameServerConfig, FetcherConfig config) {
        ConsumerClientManager consumerClientManager = ConsumerClientManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessageFetcher messageFetcher = new DefaultMessageFetcher(consumerClientManager, config);
        return new MessageFetcherWrapper(consumerClientManager, messageFetcher);
    }

    public static MessageFetcher create(ConsumerClientManager consumerClientManager, FetcherConfig config) {
        return new DefaultMessageFetcher(consumerClientManager, config);
    }
}