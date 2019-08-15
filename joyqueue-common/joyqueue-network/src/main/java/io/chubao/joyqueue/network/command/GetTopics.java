package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.Payload;
import io.chubao.joyqueue.network.transport.command.Types;

import com.google.common.base.Objects;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class GetTopics implements Payload, Types {

    private String app;
    /**
     * 0:all
     * 1:producer
     * 2:consumer
     */
    private int subscribeType = 1;
    @Override
    public int[] types() {
        return new int[]{CommandType.GET_TOPICS, JoyQueueCommandType.MQTT_GET_TOPICS.getCode()};
    }

    public String getApp() {
        return app;
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public GetTopics app(String app) {
        this.app = app;
        return this;
    }
    public GetTopics subscribeType(int subscribeType) {
        this.subscribeType = subscribeType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetTopics getTopics = (GetTopics) o;
        return Objects.equal(app, getTopics.app) &&
                Objects.equal(subscribeType, getTopics.subscribeType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(app, subscribeType);
    }

    @Override
    public String toString() {
        return "Subscribe{" +
                "app='" + app + '\'' +
                ", topic='" + subscribeType + '\'' +
                '}';
    }
}
