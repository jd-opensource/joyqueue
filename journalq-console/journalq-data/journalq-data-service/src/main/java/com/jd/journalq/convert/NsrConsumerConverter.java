package com.jd.journalq.convert;

import com.jd.journalq.common.domain.ClientType;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.domain.TopicType;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public class NsrConsumerConverter extends Converter<Consumer, com.jd.journalq.common.domain.Consumer> {
    @Override
    protected com.jd.journalq.common.domain.Consumer forward(Consumer consumer) {
        com.jd.journalq.common.domain.Consumer nsrConsumer = new com.jd.journalq.common.domain.Consumer();

        TopicName topicName = CodeConverter.convertTopic(consumer.getNamespace(),consumer.getTopic());
        nsrConsumer.setApp(CodeConverter.convertApp(consumer.getApp(), consumer.getSubscribeGroup()));
        nsrConsumer.setClientType(ClientType.valueOf(consumer.getClientType()));
        nsrConsumer.setTopicType(TopicType.valueOf(consumer.getTopicType()));
        nsrConsumer.setTopic(topicName);

        if (consumer.getConfig() != null) {
            nsrConsumer.setConsumerPolicy(com.jd.journalq.common.domain.Consumer.ConsumerPolicy.Builder.build()
                    .nearby(consumer.getConfig().isNearBy())
                    .concurrent(consumer.getConfig().getConcurrent())
                    .blackList(consumer.getConfig().getBlackList())
                    .ackTimeout(consumer.getConfig().getAckTimeout())
                    .archive(consumer.getConfig().isArchive())
                    .batchSize(Integer.valueOf(consumer.getConfig().getBatchSize()).shortValue())
                    .delay(consumer.getConfig().getDelay())
                    //.errTimes(consumer)
                    //.maxPartitionNum(consumer)
                    .paused(consumer.getConfig().isPaused())
                    .retry(consumer.getConfig().isRetry())
                    .filters(consumer.getConfig().getFilters())
                    .create());
            nsrConsumer.setRetryPolicy(RetryPolicy.Builder.build()
                    .maxRetrys(consumer.getConfig().getMaxRetrys())
                    .retryDelay(consumer.getConfig().getRetryDelay())
                    .expireTime(consumer.getConfig().getExpireTime())
                    .maxRetryDelay(consumer.getConfig().getMaxRetryDelay())
                    .useExponentialBackOff(consumer.getConfig().isUseExponentialBackOff())
                    .backOffMultiplier(consumer.getConfig().getBackOffMultiplier()).create());
        }
        return nsrConsumer;
    }

    @Override
    protected Consumer backward(com.jd.journalq.common.domain.Consumer nsrConsumer) {
        Consumer consumer = new Consumer();
        consumer.setId(nsrConsumer.getId());
        TopicName topicName = nsrConsumer.getTopic();
        consumer.setClientType(nsrConsumer.getClientType().value());
        consumer.setTopicType(nsrConsumer.getTopicType().code());
        if (nsrConsumer.getApp() != null) {
            AppName appName = AppName.parse(nsrConsumer.getApp());
            consumer.setApp(new Identity(appName.getCode()));
            consumer.setSubscribeGroup(appName.getSubscribeGroup());
        }
        if (topicName != null) {
            consumer.setTopic(new Topic(topicName.getCode()));
            consumer.setNamespace(new Namespace(topicName.getNamespace()));
        }

        ConsumerConfig consumerConfig = new ConsumerConfig();
        com.jd.journalq.common.domain.Consumer.ConsumerPolicy consumerPolicy = nsrConsumer.getConsumerPolicy();
        if (consumerPolicy != null) {
            consumerConfig.setConsumerId(nsrConsumer.getId());
            consumerConfig.setNearBy(consumerPolicy.getNearby());
            consumerConfig.setBlackList(StringUtils.join(consumerPolicy.getBlackList(), ","));
            consumerConfig.setAckTimeout(consumerPolicy.getAckTimeout());
            consumerConfig.setArchive(consumerPolicy.getArchive());
            consumerConfig.setBatchSize(consumerPolicy.getBatchSize());
            consumerConfig.setConcurrent(consumerPolicy.getConcurrent());
            consumerConfig.setDelay(consumerPolicy.getDelay());
            consumerConfig.setPaused(consumerPolicy.getPaused());
            consumerConfig.setRetry(consumerPolicy.getRetry());
            Map<String,String> map = consumerPolicy.getFilters();
            if (map !=null) {
                List<String> filterList = map.entrySet().stream().map(entry -> (entry.getKey() + ":" + entry.getValue())).collect(Collectors.toList());
                consumerConfig.setFilters(StringUtils.join(filterList, ","));
            }
        }

        RetryPolicy retryPolicy = nsrConsumer.getRetryPolicy();
        if (retryPolicy != null) {
            consumerConfig.setMaxRetrys(retryPolicy.getMaxRetrys());
            consumerConfig.setRetryDelay(retryPolicy.getRetryDelay());
            consumerConfig.setExpireTime(retryPolicy.getExpireTime());
            consumerConfig.setMaxRetryDelay(retryPolicy.getMaxRetryDelay());
            if (retryPolicy.getUseExponentialBackOff() != null) {
                consumerConfig.setUseExponentialBackOff(retryPolicy.getUseExponentialBackOff());
            }
        }
        consumer.setConfig(consumerConfig);

        return consumer;
    }
}
