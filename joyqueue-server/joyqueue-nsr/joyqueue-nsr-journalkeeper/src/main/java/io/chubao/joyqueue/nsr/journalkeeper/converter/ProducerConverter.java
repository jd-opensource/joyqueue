package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.ClientType;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.domain.ProducerDTO;
import io.chubao.joyqueue.nsr.journalkeeper.helper.JsonHelper;
import org.apache.commons.collections.CollectionUtils;

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