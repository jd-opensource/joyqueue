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
package org.joyqueue.monitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author majun8
 */
public class MqttSessionInfo extends BaseMonitorInfo {

    private String clientID;
    private Set<MqttSubscriptionInfo> subscriptions = new HashSet<>();
    private boolean cleanSession;
    private int messageAcknowledgedZoneSize;
    private long consumed;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Set<MqttSubscriptionInfo> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<MqttSubscriptionInfo> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getMessageAcknowledgedZoneSize() {
        return messageAcknowledgedZoneSize;
    }

    public void setMessageAcknowledgedZoneSize(int messageAcknowledgedZoneSize) {
        this.messageAcknowledgedZoneSize = messageAcknowledgedZoneSize;
    }

    public long getConsumed() {
        return consumed;
    }

    public void setConsumed(long consumed) {
        this.consumed = consumed;
    }
}
