package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.command.UnSubscribe;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/16
 */
public class UnSubscribeCodec implements PayloadCodec<Header, UnSubscribe>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        Short subscriptionSize = buffer.readShort();
        List<Subscription> subscriptions = new ArrayList();
        if (subscriptionSize > 0) {
            for (int i = 0; i < subscriptionSize; i++) {
                TopicName topic = TopicName.parse(Serializer.readString(buffer));
                String app = Serializer.readString(buffer);
                Subscription.Type type = Subscription.Type.valueOf(buffer.readByte());
                subscriptions.add(new Subscription(topic, app, type));
            }
        }
        return new UnSubscribe().subscriptions(subscriptions);
    }

    @Override
    public void encode(UnSubscribe payload, ByteBuf buffer) throws Exception {
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
        return CommandType.UNSUBSCRIBE;
    }
}
