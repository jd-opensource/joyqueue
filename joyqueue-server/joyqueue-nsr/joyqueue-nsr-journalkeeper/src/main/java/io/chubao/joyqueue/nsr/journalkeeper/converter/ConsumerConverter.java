/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.ClientType;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.domain.TopicType;
import io.chubao.joyqueue.nsr.journalkeeper.domain.ConsumerDTO;
import io.chubao.joyqueue.nsr.journalkeeper.helper.JsonHelper;
import io.chubao.joyqueue.toolkit.retry.RetryPolicy;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * ConsumerConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConsumerConverter {

    public static ConsumerDTO convert(Consumer consumer) {
        if (consumer == null) {
            return null;
        }

        ConsumerDTO consumerDTO = new ConsumerDTO();
        consumerDTO.setId(generateId(consumer));
        consumerDTO.setClientType(consumer.getClientType().value());
        consumerDTO.setTopicType(consumer.getTopicType().code());
        consumerDTO.setRetryPolicy(JsonHelper.toJson(consumer.getRetryPolicy()));
        consumerDTO.setConsumePolicy(JsonHelper.toJson(consumer.getConsumerPolicy()));
        consumerDTO.setLimitPolicy(JsonHelper.toJson(consumer.getLimitPolicy()));
        consumerDTO.setTopic(consumer.getTopic().getCode());
        consumerDTO.setNamespace(consumer.getTopic().getNamespace());
        consumerDTO.setApp(consumer.getApp());
        consumerDTO.setReferer(consumer.getApp());
        return consumerDTO;
    }

    protected static String generateId(Consumer consumer) {
        return String.format("%s.%s", consumer.getTopic().getFullName(), consumer.getApp());
    }

    public static Consumer convert(ConsumerDTO consumerDTO) {
        if (consumerDTO == null) {
            return null;
        }
        Consumer consumer = new Consumer();
        consumer.setTopic(TopicName.parse(consumerDTO.getTopic(), consumerDTO.getNamespace()));
        consumer.setApp(consumerDTO.getApp());
        consumer.setTopicType(TopicType.valueOf(consumerDTO.getTopicType()));
        consumer.setClientType(ClientType.valueOf(consumerDTO.getClientType()));
        consumer.setConsumerPolicy(JsonHelper.parseJson(Consumer.ConsumerPolicy.class, consumerDTO.getConsumePolicy()));
        consumer.setRetryPolicy(JsonHelper.parseJson(RetryPolicy.class, consumerDTO.getRetryPolicy()));
        consumer.setLimitPolicy(JsonHelper.parseJson(Consumer.ConsumerLimitPolicy.class, consumerDTO.getLimitPolicy()));
        return consumer;
    }

    public static List<Consumer> convert(List<ConsumerDTO> consumerDTOList) {
        if (CollectionUtils.isEmpty(consumerDTOList)) {
            return Collections.emptyList();
        }
        List<Consumer> result = Lists.newArrayListWithCapacity(consumerDTOList.size());
        for (ConsumerDTO consumerDTO : consumerDTOList) {
            result.add(convert(consumerDTO));
        }
        return result;
    }
}