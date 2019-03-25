package com.jd.journalq.common.network.command;

import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.toolkit.lang.Objects;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class UnSubscribe extends JMQPayload {
    //订阅关系
    private List<Subscription> subscriptions;

    @Override
    public int type() {
        return JMQCommandType.MQTT_UNSUBSCRIBE.getCode();
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public UnSubscribe subscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(subscriptions != null, "subscription can not be null.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnSubscribe that = (UnSubscribe) o;
        return Objects.equal(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(subscriptions);
    }

    @Override
    public String toString() {
        return "UnSubscribe{" +
                "subscriptions=" + subscriptions +
                '}';
    }
}
