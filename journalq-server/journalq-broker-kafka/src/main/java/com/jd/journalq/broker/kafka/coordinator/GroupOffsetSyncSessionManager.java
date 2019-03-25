package com.jd.journalq.broker.kafka.coordinator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.exception.LeaderNotAvailableException;
import com.jd.journalq.broker.network.support.BrokerTransportClientFactory;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.config.ClientConfig;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * GroupOffsetSyncSessionManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/9
 */
public class GroupOffsetSyncSessionManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(GroupOffsetSyncSessionManager.class);

    private KafkaConfig config;
    private TransportClient client;
    private Cache<Integer, Transport> sessions;

    public GroupOffsetSyncSessionManager(KafkaConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        client = new BrokerTransportClientFactory().create(new ClientConfig());
        sessions = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getCoordinatorOffsetSessionCache(), TimeUnit.MILLISECONDS)
                .removalListener((RemovalNotification<Integer, Transport>  notification) -> {
                    try {
                        notification.getValue().stop();
                    } catch (Exception e) {
                        logger.error("stop offset sync transport exception, id: {}", notification.getKey(), e);
                    }
                })
                .build();
    }

    @Override
    public void doStop() {
        if (client != null) {
            client.stop();
        }
    }

    public Transport getOrCreateTransport(Broker broker) {
        try {
            Transport brokerTransport = sessions.get(broker.getId(), () -> {
                Transport transport = client.createTransport(new InetSocketAddress(broker.getIp(), broker.getBackEndPort()));
                logger.info("create offset sync transport, id: {}, ip: {}, port: {}", broker.getId(), broker.getIp(), broker.getBackEndPort());
                return transport;
            });
            return brokerTransport;
        } catch (ExecutionException e) {
            throw new LeaderNotAvailableException(String.format("create offset sync transport failed, broker: {id: %s, ip: %s, port: %s}",
                    broker.getId(), broker.getIp(), broker.getBackEndPort()), e);
        }
    }
}