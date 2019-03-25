package com.jd.journalq.client.internal.producer;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.helper.NameServerHelper;
import com.jd.journalq.client.internal.producer.config.SenderConfig;
import com.jd.journalq.client.internal.producer.support.DefaultMessageSender;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManager;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManagerFactory;
import com.jd.journalq.client.internal.transport.config.TransportConfig;

/**
 * MessageSenderFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class MessageSenderFactory {

    public static MessageSender create(String address, String app, String token) {
        return create(address, app, token, null, null);
    }

    public static MessageSender create(String address, String app, String token, String region) {
        return create(address, app, token, region, null);
    }

    public static MessageSender create(String address, String app, String token, String region, String namespace) {
        NameServerConfig nameServerConfig = NameServerHelper.createConfig(address, app, token, region, namespace);
        return create(nameServerConfig);
    }

    public static MessageSender create(NameServerConfig nameServerConfig) {
        return create(nameServerConfig, new SenderConfig());
    }

    public static MessageSender create(NameServerConfig nameServerConfig, SenderConfig config) {
        return create(nameServerConfig, new TransportConfig(), config);
    }

    public static MessageSender create(NameServerConfig nameServerConfig, TransportConfig transportConfig, SenderConfig config) {
        ProducerClientManager producerClientManager = ProducerClientManagerFactory.create(nameServerConfig, transportConfig);
        DefaultMessageSender messageSender = new DefaultMessageSender(producerClientManager, config);
        return new MessageSenderWrapper(producerClientManager, messageSender);
    }

    public static MessageSender create(ProducerClientManager producerClientManager, SenderConfig config) {
        return new DefaultMessageSender(producerClientManager, config);
    }
}