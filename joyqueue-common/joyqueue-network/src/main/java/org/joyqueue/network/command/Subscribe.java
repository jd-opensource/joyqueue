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
package org.joyqueue.network.command;

import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Subscription;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class Subscribe extends JoyQueuePayload {
    private List<Subscription> subscriptions;
    private ClientType clientType;

    @Override
    public int type() {
        return JoyQueueCommandType.MQTT_SUBSCRIBE.getCode();
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public Subscribe subscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public Subscribe clientType(ClientType clientType) {
        this.clientType = clientType;
        return this;
    }

    @Override
    public void validate() {
        super.validate();
        super.validate();
        Preconditions.checkArgument(subscriptions != null && !subscriptions.isEmpty(), "subscription can not be null");
        Preconditions.checkArgument(clientType != null, "client type can not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscribe subscribe = (Subscribe) o;
        return Objects.equal(subscriptions, subscribe.subscriptions) &&
                clientType == subscribe.clientType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(subscriptions, clientType);
    }

    @Override
    public String toString() {
        return "Subscribe{" +
                "subscriptions=" + subscriptions +
                ", clientType=" + clientType +
                '}';
    }
}
