package com.jd.journalq.common.network.command;

import com.jd.journalq.common.domain.ClientType;
import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.toolkit.lang.Objects;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class Subscribe extends JMQPayload {
    private List<Subscription> subscriptions;
    private ClientType clientType;

    @Override
    public int type() {
        return JMQCommandType.MQTT_SUBSCRIBE.getCode();
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
