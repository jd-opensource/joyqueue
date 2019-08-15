package io.openmessaging.joyqueue.producer.support;

import com.google.common.collect.Lists;
import io.openmessaging.producer.SendResult;

import java.util.List;

/**
 * SendResultConverter
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class SendResultConverter {

    public static List<SendResult> convert(List<io.chubao.joyqueue.client.internal.producer.domain.SendResult> sendResults) {
        List<SendResult> result = Lists.newArrayListWithCapacity(sendResults.size());
        for (io.chubao.joyqueue.client.internal.producer.domain.SendResult sendResult : sendResults) {
            result.add(convert(sendResult));
        }
        return result;
    }

    public static SendResult convert(io.chubao.joyqueue.client.internal.producer.domain.SendResult sendResult) {
        return new SendResultAdapter(sendResult);
    }
}