package com.jd.journalq.client.internal.consumer.converter;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.common.network.command.RetryType;

import java.util.List;

/**
 * ConsumeMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class ConsumeMessageConverter {

    public static List<ConsumeReply> convertToReply(List<ConsumeMessage> messages, RetryType retryType) {
        List<ConsumeReply> result = Lists.newArrayListWithCapacity(messages.size());
        for (ConsumeMessage message : messages) {
            result.add(new ConsumeReply(message.getPartition(), message.getIndex(), retryType));
        }
        return result;
    }
}