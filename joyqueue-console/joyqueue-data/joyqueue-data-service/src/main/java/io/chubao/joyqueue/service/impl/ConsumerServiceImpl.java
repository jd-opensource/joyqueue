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
package io.chubao.joyqueue.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.chubao.joyqueue.convert.CodeConverter;
import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.AppName;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.Consumer;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.query.QApplication;
import io.chubao.joyqueue.model.query.QConsumer;
import io.chubao.joyqueue.nsr.ConsumerNameServerService;
import io.chubao.joyqueue.nsr.TopicNameServerService;
import io.chubao.joyqueue.service.ApplicationService;
import io.chubao.joyqueue.service.ConsumerService;
import io.chubao.joyqueue.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("consumerService")
public class ConsumerServiceImpl  implements ConsumerService {
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
        Preconditions.checkArgument(consumer!=null && consumer.getTopic()!=null, "invalid consumer arg");

        try {
            //Find topic
            Topic topic = topicNameServerService.findById(consumer.getTopic().getId());
            consumer.setTopic(topic);
            consumer.setNamespace(topic.getNamespace());

            return consumerNameServerService.add(consumer);
        }catch (Exception e){
            String errorMsg = String.format("add consumer with nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
    }

    @Override
    public Consumer findById(String s) throws Exception {
        return consumerNameServerService.findById(s);
    }

    @Override
    public PageResult<Consumer> findByQuery(QPageQuery<QConsumer> query) throws Exception {
        User user = LocalSession.getSession().getUser();
        if (query.getQuery() != null && query.getQuery().getApp() != null){
            query.getQuery().setReferer(query.getQuery().getApp().getCode());
            query.getQuery().setApp(null);
        }
        if (user.getRole() == User.UserRole.NORMAL.value()) {
            QApplication qApplication = new QApplication();
            qApplication.setUserId(user.getId());
            qApplication.setAdmin(false);
            List<Application> applicationList = applicationService.findByQuery(new ListQuery<>(qApplication));
            if (applicationList == null || applicationList.size() <=0 ) return PageResult.empty();
            List<String> appCodes = applicationList.stream().map(application -> application.getCode()).collect(Collectors.toList());
            query.getQuery().setAppList(appCodes);
        }
        return consumerNameServerService.findByQuery(query);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int delete(Consumer consumer) {
        //Validate
        checkArgument(consumer);
        try {
            consumerNameServerService.delete(consumer);
        }catch (Exception e){
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
        }catch (Exception e){
            String errorMsg = String.format("update consumer by nameServer failed, consumer is %s.", JSON.toJSONString(consumer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return 1;
    }

    @Override
    public List<Consumer> findByQuery(QConsumer query) throws Exception {
        return consumerNameServerService.findByQuery(query);
    }

    @Override
    public Consumer findByTopicAppGroup(String namespace, String topic, String app, String group) {
        try {
            QConsumer qConsumer = new QConsumer();
//            qConsumer.setReferer(app);
            qConsumer.setApp(new Identity(CodeConverter.convertApp(new Identity(app), group)));
            //consumer表没存group
            if (group !=null) {
                qConsumer.setApp(new Identity(CodeConverter.convertApp(new Identity(app), group)));
            }
            qConsumer.setNamespace(namespace);
            qConsumer.setTopic(new Topic(topic));
            List<Consumer> consumerList = consumerNameServerService.findByQuery(qConsumer);
            if (consumerList == null || consumerList.size() <= 0)return null;
            return consumerList.get(0);
        } catch (Exception e) {
            logger.error("findByTopicAppGroup error",e);
            throw new RuntimeException("findByTopicAppGroup error",e);
        }
    }

    @Override
    public List<String> findAllSubscribeGroups() {
        try {
            return consumerNameServerService.findAllSubscribeGroups();
        } catch (Exception e) {
            logger.error("findAllSubscribeGroups error",e);
            throw new RuntimeException("findAllSubscribeGroups error",e);
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
            if (applicationList == null || applicationList.size() <=0 ) return Lists.newArrayList();
            List<String> appCodes = applicationList.stream().map(application -> application.getCode()).collect(Collectors.toList());
            query.setAppList(appCodes);
        }
        List<Consumer> consumers = findByQuery(query);
        List<String> apps = consumers.stream().map(m-> AppName.parse(m.getApp().getCode(),m.getSubscribeGroup()).getFullName()).collect(Collectors.toList());
        return apps;
    }

    private void checkArgument(Consumer consumer) {
        Preconditions.checkArgument(consumer != null && consumer.getId() != null, "invalidate consumer arg.");
    }

}
