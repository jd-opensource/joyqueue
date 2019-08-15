package io.openmessaging.joyqueue.consumer;

import io.openmessaging.consumer.Consumer;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * ExtensionConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/4
 */
public interface ExtensionConsumer extends Consumer {

    Message receive(short partition, long timeout);

    List<Message> batchReceive(short partition, long timeout);

    Message receive(short partition, long index, long timeout);

    List<Message> batchReceive(short partition, long index, long timeout);
}