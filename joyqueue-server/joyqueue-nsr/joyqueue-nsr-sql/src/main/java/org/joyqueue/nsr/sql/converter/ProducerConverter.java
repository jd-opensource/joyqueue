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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.domain.ProducerDTO;
import org.joyqueue.nsr.sql.helper.JsonHelper;

import java.util.Collections;
import java.util.List;

/**
 * ProducerConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ProducerConverter {

    public static ProducerDTO convert(Producer producer) {
        if (producer == null) {
            return null;
        }

        ProducerDTO producerDTO = new ProducerDTO();
        producerDTO.setId(generateId(producer));
        producerDTO.setClientType(producer.getClientType().value());
        producerDTO.setProducePolicy(JsonHelper.toJson(producer.getProducerPolicy()));
        producerDTO.setLimitPolicy(JsonHelper.toJson(producer.getLimitPolicy()));
        producerDTO.setTopic(producer.getTopic().getCode());
        producerDTO.setNamespace(producer.getTopic().getNamespace());
        producerDTO.setApp(producer.getApp());
        return producerDTO;
    }

    protected static String generateId(Producer producer) {
        return String.format("%s.%s", producer.getTopic().getFullName(), producer.getApp());
    }

    public static Producer convert(ProducerDTO producerDTO) {
        if (producerDTO == null) {
            return null;
        }
        Producer producer = new Producer();
        producer.setTopic(TopicName.parse(producerDTO.getTopic(), producerDTO.getNamespace()));
        producer.setApp(producerDTO.getApp());
        producer.setClientType(ClientType.valueOf(producerDTO.getClientType()));
        producer.setProducerPolicy(JsonHelper.parseJson(Producer.ProducerPolicy.class, producerDTO.getProducePolicy()));
        producer.setLimitPolicy(JsonHelper.parseJson(Producer.ProducerLimitPolicy.class, producerDTO.getLimitPolicy()));
        return producer;
    }

    public static List<Producer> convert(List<ProducerDTO> producerDTOList) {
        if (CollectionUtils.isEmpty(producerDTOList)) {
            return Collections.emptyList();
        }
        List<Producer> result = Lists.newArrayListWithCapacity(producerDTOList.size());
        for (ProducerDTO producerDTO : producerDTOList) {
            result.add(convert(producerDTO));
        }
        return result;
    }
}