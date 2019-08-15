package io.chubao.joyqueue.client.internal.consumer;

import io.chubao.joyqueue.client.internal.consumer.config.FetcherConfig;
import io.chubao.joyqueue.client.internal.consumer.support.DefaultMessageFetcher;
import io.chubao.joyqueue.client.internal.consumer.support.MessageFetcherWrapper;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManagerFactory;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;

/**
 * MessageFetcherFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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