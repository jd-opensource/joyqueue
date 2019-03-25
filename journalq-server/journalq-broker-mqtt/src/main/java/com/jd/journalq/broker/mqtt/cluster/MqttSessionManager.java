package com.jd.journalq.broker.mqtt.cluster;

import com.jd.journalq.broker.mqtt.session.MqttSession;
import com.jd.journalq.broker.mqtt.subscriptions.TopicFilter;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.broker.mqtt.subscriptions.MqttSubscription;
import com.jd.journalq.toolkit.service.Service;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author majun8
 */
public class MqttSessionManager extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(MqttSessionManager.class);

    private MqttConnectionManager connectionManager;
    private final ConcurrentMap<String, MqttSession> sessions = new ConcurrentHashMap<>();
    private NameService nameService;

    public MqttSessionManager(BrokerContext brokerContext, MqttConnectionManager connectionManager) {
        this.nameService = brokerContext.getNameService();
        this.connectionManager = connectionManager;
    }

    public MqttSession getSession(String clientID) {
        MqttSession session = sessions.get(clientID);

        if (session == null) {
            LOG.error("Can't find the session for client: <{}>", clientID);
            throw new RuntimeException("Can't find the session for client <" + clientID + ">");
        }
        return session;
    }

    public void addSession(String clientID, boolean cleanSession) {
        if (sessions.containsKey(clientID)) {
            MqttSession session = sessions.get(clientID);
            if (session.isCleanSession() != cleanSession) {
                session.setCleanSession(cleanSession);
            }
        } else {
            MqttSession innerSession = new MqttSession(clientID, cleanSession);
            sessions.put(clientID, innerSession);
        }
        if (!cleanSession) {
            MqttSession session = sessions.get(clientID);
            String clientGroupName = connectionManager.getConnection(clientID).getClientGroupName();
            Set<String> topicNames = nameService.getTopics(clientGroupName, Subscription.Type.CONSUMPTION);
            if (topicNames != null && topicNames.size() > 0) {
                for (String topic : topicNames) {
                    MqttSubscription subscription = new MqttSubscription(clientID, new TopicFilter(topic), MqttQoS.AT_LEAST_ONCE);
                    session.addSubscription(subscription);
                }
                LOG.info("Persistent client group: <{}>, recovery session: {}", clientGroupName, session);
            }
        }
    }

    public void removeSession(String clientID) {
        MqttSession session = sessions.get(clientID);
        if (session != null) {
            if (session.isCleanSession()) {
                String clientGroupName = connectionManager.getConnection(clientID).getClientGroupName();
                Set<MqttSubscription> subscriptions = session.listSubsciptions();
                List<Subscription> unSubscriptionList = new ArrayList<>(subscriptions.size());
                for (MqttSubscription subscription : subscriptions) {
                    unSubscriptionList.add(new Subscription(TopicName.parse(subscription.getTopicFilter().toString()), clientGroupName, Subscription.Type.CONSUMPTION));
                }
                nameService.unSubscribe(unSubscriptionList);
            }
            sessions.remove(clientID);
        }
    }

    public boolean contains(String clientID) {
        return sessions.containsKey(clientID);
    }

    public Map<String, MqttSession> listSessions() {
        return sessions;
    }
}
