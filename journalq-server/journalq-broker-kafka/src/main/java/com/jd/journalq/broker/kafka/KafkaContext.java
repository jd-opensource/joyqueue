package com.jd.journalq.broker.kafka;

import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.handler.ratelimit.KafkaRateLimitHandlerFactory;
import com.jd.journalq.broker.kafka.session.KafkaConnectionManager;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.kafka.coordinator.GroupBalanceHandler;
import com.jd.journalq.broker.kafka.coordinator.GroupBalanceManager;
import com.jd.journalq.broker.kafka.coordinator.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.GroupOffsetHandler;
import com.jd.journalq.broker.kafka.coordinator.GroupOffsetManager;
import com.jd.journalq.broker.kafka.coordinator.KafkaCoordinatorGroupManager;

/**
 * KafkaContext
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class KafkaContext {

    private KafkaConfig config;
    private KafkaConnectionManager connectionManager;
    private KafkaCoordinatorGroupManager groupMetadataManager;
    private GroupOffsetManager groupOffsetManager;
    private GroupBalanceManager groupBalanceManager;
    private GroupOffsetHandler groupOffsetHandler;
    private GroupBalanceHandler groupBalanceHandler;
    private GroupCoordinator groupCoordinator;
    private KafkaRateLimitHandlerFactory rateLimitHandlerFactory;
    private BrokerContext brokerContext;

    public KafkaContext(KafkaConfig config, KafkaConnectionManager connectionManager, KafkaCoordinatorGroupManager groupMetadataManager, GroupOffsetManager groupOffsetManager, GroupBalanceManager groupBalanceManager,
                        GroupOffsetHandler groupOffsetHandler, GroupBalanceHandler groupBalanceHandler, GroupCoordinator groupCoordinator, KafkaRateLimitHandlerFactory rateLimitHandlerFactory, BrokerContext brokerContext) {
        this.config = config;
        this.connectionManager = connectionManager;
        this.groupMetadataManager = groupMetadataManager;
        this.groupOffsetManager = groupOffsetManager;
        this.groupBalanceManager = groupBalanceManager;
        this.groupOffsetHandler = groupOffsetHandler;
        this.groupBalanceHandler = groupBalanceHandler;
        this.groupCoordinator = groupCoordinator;
        this.rateLimitHandlerFactory = rateLimitHandlerFactory;
        this.brokerContext = brokerContext;
    }

    public KafkaConfig getConfig() {
        return config;
    }

    public KafkaConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public KafkaCoordinatorGroupManager getGroupMetadataManager() {
        return groupMetadataManager;
    }

    public GroupOffsetManager getGroupOffsetManager() {
        return groupOffsetManager;
    }

    public GroupBalanceManager getGroupBalanceManager() {
        return groupBalanceManager;
    }

    public GroupOffsetHandler getGroupOffsetHandler() {
        return groupOffsetHandler;
    }

    public GroupBalanceHandler getGroupBalanceHandler() {
        return groupBalanceHandler;
    }

    public GroupCoordinator getGroupCoordinator() {
        return groupCoordinator;
    }

    public KafkaRateLimitHandlerFactory getRateLimitHandlerFactory() {
        return rateLimitHandlerFactory;
    }

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }
}