package io.openmessaging.joyqueue.consumer.message;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * MessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class MessageConverter {

    public static List<Message> convertMessages(List<ConsumeMessage> consumeMessages) {
        List<Message> result = Lists.newArrayListWithCapacity(consumeMessages.size());
        for (ConsumeMessage consumeMessage : consumeMessages) {
            result.add(convertMessage(consumeMessage));
        }
        return result;
    }

    public static Message convertMessage(ConsumeMessage consumeMessage) {
        return new MessageAdapter(consumeMessage);
    }
}