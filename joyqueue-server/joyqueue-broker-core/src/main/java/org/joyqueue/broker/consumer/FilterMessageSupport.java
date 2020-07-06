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
package org.joyqueue.broker.consumer;

import com.alibaba.fastjson.JSON;
import com.jd.laf.extension.ExtensionManager;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.filter.FilterCallback;
import org.joyqueue.broker.consumer.filter.FilterPipeline;
import org.joyqueue.broker.consumer.filter.MessageFilter;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.nsr.event.UpdateConsumerEvent;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.security.Hex;
import org.joyqueue.toolkit.security.Md5;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消息过滤
 * <p>
 * Created by chengzhiliang on 2018/8/22.
 */
class FilterMessageSupport {

    private final Logger logger = LoggerFactory.getLogger(FilterMessageSupport.class);
    // 集群管理
    private ClusterManager clusterManager;
    // 用户的消息过滤管道缓存
    private ConcurrentMap</* consumerId */String, /* 过滤管道 */FilterPipeline<MessageFilter>> filterRuleCache = new ConcurrentHashMap<>();

    FilterMessageSupport(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;

        // 添加消费者信息更新事件
        clusterManager.addListener(new updateConsumeListener());
    }


    /**
     * 根据用户设置的过滤规则过滤消息
     *
     * @param consumer       消费者
     * @param byteBuffers    消息缓存集合
     * @param filterCallback 过滤回调函数，用于处理被过滤消息的应答问题
     * @return
     * @throws JoyQueueException
     */
    public List<ByteBuffer> filter(Consumer consumer, List<ByteBuffer> byteBuffers, FilterCallback filterCallback) throws JoyQueueException {
        FilterPipeline<MessageFilter> filterPipeline = filterRuleCache.get(consumer.getId());
        if (filterPipeline == null) {
            filterPipeline = createFilterPipeline(consumer.getConsumerPolicy());
            filterRuleCache.putIfAbsent(consumer.getId(), filterPipeline);
        }
        List<ByteBuffer> result = filterPipeline.execute(byteBuffers, filterCallback);
        return result;
    }

    /**
     * 根据用户配置消费策略构建顾虑管道
     *
     * @param consumerPolicy 用户消费策略
     * @return 消息过滤管道
     * @throws JoyQueueException
     */
    private FilterPipeline<MessageFilter> createFilterPipeline(Consumer.ConsumerPolicy consumerPolicy) throws JoyQueueException {
        Map<String, String> filterRule = null;
        if (consumerPolicy != null) {
            filterRule = consumerPolicy.getFilters();
        }

        String pipelineId = generatePipelineId(filterRule);
        FilterPipeline<MessageFilter> filterPipeline = new FilterPipeline<>(pipelineId);

        if (MapUtils.isNotEmpty(filterRule)) {
            Set<Map.Entry<String, String>> entries = filterRule.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String type = entry.getKey(); // type
                String content = entry.getValue();// content;
                MessageFilter newMessageFilter = ExtensionManager.getOrLoadExtension(MessageFilter.class, type);
                newMessageFilter.setRule(content);
                filterPipeline.register(newMessageFilter);
            }
        }

        return filterPipeline;
    }

    /**
     * 生成管道标号
     *
     * @param filterRule 过滤规则
     * @return 管道编号
     * @throws JoyQueueException
     */
    private String generatePipelineId(Map<String, String> filterRule) throws JoyQueueException {
        if (MapUtils.isEmpty(filterRule)) {
            return null;
        }
        String jsonString = JSON.toJSONString(filterRule);
        try {
            byte[] encrypt = Md5.INSTANCE.encrypt(jsonString.getBytes("utf-8"), null);
            return Hex.encode(encrypt);
        } catch (Exception e) {
            logger.error("generate filter pipeline error.", e);
            throw new JoyQueueException(e, JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
        }
    }

    /**
     * 更新消费者对于的过滤管道
     *
     * @param topic
     * @param app
     */
    private void updateFilterRuleCache(TopicName topic, String app) {
        try {
            Consumer.ConsumerPolicy consumerPolicy = clusterManager.getConsumerPolicy(topic, app);
            Map<String, String> filters = consumerPolicy.getFilters();
            String pipelineId = generatePipelineId(filters);
            FilterPipeline<MessageFilter> pipeline = filterRuleCache.get(getConsumeId(topic, app));
            if (pipeline != null && StringUtils.equals(pipelineId, pipeline.getId())) {
                // id相同说明过滤管道已经存在，不需要重复创建
                logger.info("FilterPipeline is already exist, topic:[{}], app:[{}], filers:[{}]", topic, app, JSON.toJSON(filters));
                return;
            }

            FilterPipeline<MessageFilter> filterPipeline = createFilterPipeline(consumerPolicy);
            filterRuleCache.put(getConsumeId(topic, app), filterPipeline);
        } catch (Exception ex) {
            logger.error("Update Message filter cache error.", ex);
        }
    }

    /**
     * 生成用户编号
     *
     * @param topic 主题
     * @param app   应用
     * @return 用户编号
     */
    private String getConsumeId(TopicName topic, String app) {
        // copy from Consumer.getId();
        return new StringBuilder(30).append(topic.getFullName()).append(".").append(app).toString();
    }

    /**
     * 监听消费配置变化，更新过滤管道
     */
    class updateConsumeListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            if (event.getEventType() == EventType.UPDATE_CONSUMER) {
                UpdateConsumerEvent updateConsumerEvent = (UpdateConsumerEvent) event;
                logger.info("listen update consume event to update filter pipeline.");

                // 更新消息过滤管道
                updateFilterRuleCache(updateConsumerEvent.getTopic(), updateConsumerEvent.getNewConsumer().getApp());
            }
        }
    }
}
