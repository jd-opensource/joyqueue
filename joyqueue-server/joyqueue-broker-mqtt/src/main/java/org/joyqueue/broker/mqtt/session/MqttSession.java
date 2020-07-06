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
package org.joyqueue.broker.mqtt.session;

import org.joyqueue.broker.mqtt.subscriptions.MqttSubscription;
import org.joyqueue.message.BrokerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author majun8
 */
public class MqttSession implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(MqttSession.class);
    private static final long serialVersionUID = -1L;

    private final String clientID;
    private final Set<MqttSubscription> subscriptions = new HashSet<>();
    private boolean cleanSession;

    private final MessageAcknowledgedZone messageAcknowledgedZone = new MessageAcknowledgedZone();

    public MqttSession(String clientID, boolean cleanSession) {
        this.clientID = clientID;
        this.cleanSession = cleanSession;
    }

    public void addSubscription(MqttSubscription newSubscription) {
        if (newSubscription == null) {
            LOG.error("Add null subscription for session");
            return;
        }

        subscriptions.add(newSubscription);
    }

    public void removeSubscription(MqttSubscription subscription) {
        LOG.debug("Remove subscription topic filter: {} for clientID: {}", subscription.getTopicFilter(), clientID);
        subscriptions.remove(subscription);
    }

    public Set<MqttSubscription> listSubsciptions() {
        return subscriptions;
    }

    public String getClientID() {
        return clientID;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public MessageAcknowledgedZone getMessageAcknowledgedZone() {
        return messageAcknowledgedZone;
    }

    public class MessageAcknowledgedZone {
        final Map<Integer, BrokerMessage> acknowledgedMap = Collections.synchronizedMap(new HashMap<>());

        public BrokerMessage acquireAcknowledgedMessage(Integer packageId) {
            BrokerMessage brokerMessage = acknowledgedMap.remove(packageId);
            if (brokerMessage == null) {
                LOG.error("Can't find the message for client: <{}> publish ack packageId: <{}>", clientID, packageId);
                throw new RuntimeException("Can't find the session message for client <" + clientID + ">");
            }

            return brokerMessage;
        }

        public int acquireAcknowledgedPosition(BrokerMessage brokerMessage) {
            int maxId = acknowledgedMap.keySet().isEmpty() ? 0 : Collections.max(acknowledgedMap.keySet());
            int nextPacketId = (maxId + 1) % 0xFFFF;
            acknowledgedMap.put(nextPacketId, brokerMessage);

            return nextPacketId;
        }

        public int zoneSize() {
            return acknowledgedMap.size();
        }

        @Override
        public String toString() {
            return "MessageAcknowledgedZone{" +
                    "acknowledgedMap.size()=" + acknowledgedMap.size() +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MqttSession{" +
                "clientID='" + clientID + '\'' +
                ", subscriptions=" + subscriptions +
                ", cleanSession=" + cleanSession +
                '}';
    }
}
