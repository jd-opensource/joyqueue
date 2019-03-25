package com.jd.journalq.network.codec;

import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.Subscription;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.command.*;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.command.Subscribe;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wylixiaobin
 * Date: 2018/10/16
 */
public class SubscribeCodec implements PayloadCodec<Header, Subscribe>, Type {
    @Override
    public Subscribe decode(Header header, ByteBuf buffer) throws Exception {
        byte clientType = buffer.readByte();
        short subscriptionSize = buffer.readShort();
        List<Subscription> subscriptions = new ArrayList();
        for (int i = 0; i < subscriptionSize; i++) {
            TopicName topic = TopicName.parse(Serializer.readString(buffer));
            String app = Serializer.readString(buffer);
            Subscription.Type type = Subscription.Type.valueOf(buffer.readByte());
            subscriptions.add(new Subscription(topic, app, type));
        }

        return new Subscribe().subscriptions(subscriptions).clientType(ClientType.valueOf(clientType));
    }

    @Override
    public void encode(Subscribe payload, ByteBuf buffer) throws Exception {
        buffer.writeByte(payload.getClientType().value());
        List<Subscription> subscriptions = payload.getSubscriptions();
        int subscriptionSize = subscriptions == null ? 0 : subscriptions.size();
        buffer.writeShort(subscriptionSize);

        if (subscriptionSize > 0) {
            for (Subscription subscription : subscriptions) {
                Serializer.write(subscription.getTopic().getFullName(), buffer);
                Serializer.write(subscription.getApp(), buffer);
                buffer.writeByte(subscription.getType().getValue());
            }
        }
    }

    @Override
    public int type() {
        return CommandType.SUBSCRIBE;
    }
}
