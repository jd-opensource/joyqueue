/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joyqueue.convert.CodeConverter;
import org.joyqueue.domain.TopicName;
import org.joyqueue.model.ListQuery;
import org.joyqueue.model.domain.AppName;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.Consumer;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QApplication;
import org.joyqueue.model.query.QConsumer;
import org.joyqueue.nsr.ConsumerNameServerService;
import org.joyqueue.nsr.TopicNameServerService;
import org.joyqueue.service.ApplicationService;
import org.joyqueue.service.ConsumerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("consumerService")
public class ConsumerServiceImpl implements ConsumerService {
    private final Logger logger = LoggerFactory.getLogger(ConsumerServiceImpl.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private TopicNameServerService topicNameServerService;

    @Autowired
    private ConsumerNameServerService consumerNameServerService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int add(Consumer consumer) {
        Preconditions.checkArgument(consumer != null && consumer.getTopic() != null, "invalid consumer arg");

        try {
            //Find topic
            Topic topic = topicNameServerService.findById(consumer.getTopic().getId());
            if (topic.getType() == Topic.TOPIC_TYPE_SEQUENTIAL && consumer.getTopicType() == Topic.TOPIC_TYPE_BROADCAST) {
                throw new IllegalArgumentException("broadcast subscriptions are not allowed for sequential topics");
            }

            consumer.setTopic(topic);
            consumer.setNamespace(topic.getNamespace());

            return consumerNameServerService.add(consumer);
        } catch (Exception e) {
            String errorMsg = String.format("add consumer with nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
    }

    @Override
    public Consumer findById(String s) throws Exception {
        return fillConsumer(consumerNameServerService.findById(s));
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int delete(Consumer consumer) {
        //Validate
        checkArgument(consumer);
        try {
            consumerNameServerService.delete(consumer);
        } catch (Exception e) {
            String errorMsg = String.format("remove consumer status by nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int update(Consumer consumer) {
        //Validate
        checkArgument(consumer);
        try {
            consumerNameServerService.update(consumer);
        } catch (Exception e) {
            String errorMsg = String.format("update consumer by nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Override
    public Consumer findByTopicAppGroup(String namespace, String topic, String app, String group) {
        try {
            TopicName topicName = TopicName.parse(topic);
            if (StringUtils.isNotBlank(group)) {
                return fillConsumer(consumerNameServerService.findByTopicAndApp(topicName.getCode(), namespace, CodeConverter.convertApp(new Identity(app), group)));
            } else {
                return fillConsumer(consumerNameServerService.findByTopicAndApp(topicName.getCode(), namespace, app));
            }
        } catch (Exception e) {
            logger.error("findByTopicAppGroup error", e);
            throw new RuntimeException("findByTopicAppGroup error", e);
        }
    }

    @Override
    public List<Consumer> findByTopic(String topic, String namespace) throws Exception {
        TopicName topicName = TopicName.parse(topic);
        if (StringUtils.isNoneBlank(topicName.getNamespace()) && StringUtils.isBlank(namespace)) {
            namespace = topicName.getNamespace();
        }
        return fillConsumers(consumerNameServerService.findByTopic(topic, namespace));
    }

    @Override
    public List<Consumer> findByApp(String app) throws Exception {
        return fillConsumers(consumerNameServerService.findByApp(app));
    }

    @Override
    public List<String> findAllSubscribeGroups() {
        try {
            return consumerNameServerService.findAllSubscribeGroups();
        } catch (Exception e) {
            logger.error("findAllSubscribeGroups error", e);
            throw new RuntimeException("findAllSubscribeGroups error", e);
        }
    }

    @Override
    public List<String> findAppsByTopic(String topic) throws Exception {
        User user = LocalSession.getSession().getUser();
        QConsumer query = new QConsumer(new Topic(topic));
        if (user.getRole() == User.UserRole.NORMAL.value()) {
            QApplication qApplication = new QApplication();
            qApplication.setUserId(user.getId());
            qApplication.setAdmin(false);
            List<Application> applicationList = applicationService.findByQuery(new ListQuery<>(qApplication));
            if (applicationList == null || applicationList.size() <= 0) return Lists.newArrayList();
            List<String> appCodes = applicationList.stream().map(application -> application.getCode()).collect(Collectors.toList());
            query.setAppList(appCodes);
        }
        if (query.getAppList() == null ) {
            query.setAppList(Collections.emptyList());
        }
        List<Consumer> consumers = Lists.newLinkedList();
        for (String app : query.getAppList()) {
            List appConsumers = consumerNameServerService.findByApp(app);
            if (CollectionUtils.isNotEmpty(appConsumers)) {
                consumers.addAll(appConsumers);
            }
        }

        List<String> apps = consumers.stream().map(m -> AppName.parse(m.getApp().getCode(), m.getSubscribeGroup()).getFullName()).collect(Collectors.toList());
        return apps;
    }

    private void checkArgument(Consumer consumer) {
        Preconditions.checkArgument(consumer != null && consumer.getId() != null, "invalidate consumer arg.");
    }

    protected List<Consumer> fillConsumers(List<Consumer> consumers) {
        if (CollectionUtils.isEmpty(consumers)) {
            return consumers;
        }
        for (Consumer consumer : consumers) {
            fillConsumer(consumer);
        }
        return consumers;
    }

    protected Consumer fillConsumer(Consumer consumer) {
        if (consumer == null) {
            return null;
        }
        Identity app = consumer.getApp();
        if (app == null || StringUtils.isBlank(app.getCode())) {
            return consumer;
        }

        Application application = applicationService.findByCode(CodeConverter.convertAppFullName(app.getCode()).getCode());
        if (application != null) {
            app.setId(application.getId());
            app.setName(application.getName());
            consumer.setOwner(application.getOwner());
        }
        return consumer;
    }

}
