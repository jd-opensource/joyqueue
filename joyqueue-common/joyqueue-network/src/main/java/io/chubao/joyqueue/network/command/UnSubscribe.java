package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class UnSubscribe extends JoyQueuePayload {
    //订阅关系
    private List<Subscription> subscriptions;

    @Override
    public int type() {
        return JoyQueueCommandType.MQTT_UNSUBSCRIBE.getCode();
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
