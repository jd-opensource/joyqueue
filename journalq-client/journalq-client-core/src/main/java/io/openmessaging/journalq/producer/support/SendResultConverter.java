package io.openmessaging.journalq.producer.support;

import com.google.common.collect.Lists;
import io.openmessaging.producer.SendResult;

import java.util.List;

/**
 * SendResultConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class SendResultConverter {

    public static List<SendResult> convert(List<com.jd.journalq.client.internal.producer.domain.SendResult> sendResults) {
        List<SendResult> result = Lists.newArrayListWithCapacity(sendResults.size());
        for (com.jd.journalq.client.internal.producer.domain.SendResult sendResult : sendResults) {
            result.add(convert(sendResult));
        }
        return result;
    }

    public static SendResult convert(com.jd.journalq.client.internal.producer.domain.SendResult sendResult) {
        return new SendResultAdapter(sendResult);
    }
}