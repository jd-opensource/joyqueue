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
package org.joyqueue.service.impl;

import com.google.common.base.Preconditions;
import org.joyqueue.exception.ServiceException;
import org.joyqueue.exception.ValidationException;
import org.joyqueue.model.ListQuery;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.model.domain.Consumer;
import org.joyqueue.model.domain.Producer;
import org.joyqueue.model.domain.TopicUnsubscribedApplication;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QApplication;
import org.joyqueue.nsr.AppTokenNameServerService;
import org.joyqueue.nsr.ConsumerNameServerService;
import org.joyqueue.nsr.ProducerNameServerService;
import org.joyqueue.repository.ApplicationRepository;
import org.joyqueue.service.ApplicationService;
import org.joyqueue.service.ApplicationUserService;
import org.joyqueue.service.UserService;
import org.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yangyang115 on 18-7-27.
 */
@Service("applicationService")
public class ApplicationServiceImpl extends PageServiceSupport<Application, QApplication,ApplicationRepository> implements ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private ProducerNameServerService producerNameServerService;
    @Autowired
    private ConsumerNameServerService consumerNameServerService;
    @Autowired
    private ApplicationUserService applicationUserService;
    @Autowired
    private AppTokenNameServerService appTokenNameServerService;
    @Autowired
    private UserService userService;

    @Override
    public int add(Application app) {
        //Validate unique
        Application apps = findByCode(app.getCode());
        if (NullUtil.isNotEmpty(apps)) {
            throw new ValidationException(ValidationException.UNIQUE_EXCEPTION_STATUS, getUniqueExceptionMessage());
        }
        //fill owner_id
        if (app.getOwner() == null) {
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, "应用负责人不能为空!");
        }
        if (app.getOwner().getId() == null && app.getOwner().getCode() != null) {
            User user = userService.findByCode(app.getOwner().getCode());
            if (user != null) {
                app.getOwner().setId(user.getId());
            } else {
                throw new ValidationException(ValidationException.NOT_FOUND_EXCEPTION_STATUS, "owner|不存在");
            }
        }
        //Add
        return super.add(app);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int delete(final Application app) {
        try {
            //validate topic related producers and consumers
            Preconditions.checkArgument(NullUtil.isEmpty(producerNameServerService.findByApp(app.getCode())),
                    String.format("app %s exists related producers", app.getCode()));
            Preconditions.checkArgument(NullUtil.isEmpty(consumerNameServerService.findByApp(app.getCode())),
                    String.format("app %s exists related consumers", app.getCode()));
            //delete related app users
            applicationUserService.deleteByAppId(app.getId());
            //delete related app tokens
            List<ApplicationToken> tokens = appTokenNameServerService.findByApp(app.getCode());
            if (NullUtil.isNotEmpty(tokens)) {
                for (ApplicationToken t : tokens) {
                    appTokenNameServerService.delete(t);
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            String msg = "delete application error. ";
            logger.error(msg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, msg, e);
        }
        //delete app
        return super.delete(app);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int update(Application model) {
        return super.update(model);
    }

    @Override
    public Application findByCode(final String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        return repository.findByCode(code);
    }

    @Override
    public PageResult<Application> findSubscribedByQuery(QPageQuery<QApplication> query) {
        if (query == null || query.getQuery() == null) {
            return PageResult.empty();
        }
        ListQuery<QApplication> listQuery = new ListQuery<>();
        listQuery.setQuery(query.getQuery());
        List<Application> applicationList = findByQuery(listQuery);
//        List<String> appList = getSubscribeList(query.getQuery());
//        if (applicationList != null && appList != null) {
//            applicationList = applicationList.stream().filter(application -> appList.contains(application.getCode())).collect(Collectors.toList());
//        }
        return new PageResult<>(query.getPagination(),applicationList);
    }
    //

    @Override
    public PageResult<TopicUnsubscribedApplication> findTopicUnsubscribedByQuery(QPageQuery<QApplication> query) {
        if (query == null || query.getQuery() == null) {
            return PageResult.empty();
        }
        if (query.getQuery() == null || query.getQuery().getSubscribeType() == null || query.getQuery().getTopic() == null
                || query.getQuery().getTopic().getCode() == null) {
            throw new ServiceException(ServiceException.BAD_REQUEST, "bad QApplication query argument.");
        }
        query.getQuery().setNoInCodes(getSubscribeList(query.getQuery()));
        PageResult<Application> applicationPageResult = repository.findUnsubscribedByQuery(query);
        if (NullUtil.isEmpty(applicationPageResult.getResult())) {
            return PageResult.empty();
        }
        return new PageResult(applicationPageResult.getPagination(), applicationPageResult.getResult().stream().map(app -> {
            TopicUnsubscribedApplication topicUnsubscribedApp = new TopicUnsubscribedApplication(app);
            topicUnsubscribedApp.setTopicCode(query.getQuery().getTopic().getCode());
            topicUnsubscribedApp.setSubscribeType(query.getQuery().getSubscribeType());
            if (query.getQuery().getSubscribeType() == Consumer.CONSUMER_TYPE) {
                //find consumer list by topic and app refer, then set showDefaultSubscribeGroup property
                try {
                    Consumer consumer = consumerNameServerService.findByTopicAndApp(query.getQuery().getTopic().getCode(), query.getQuery().getTopic().getNamespace().getCode(), app.getCode());
                    topicUnsubscribedApp.setSubscribeGroupExist(consumer != null);
                } catch (Exception e) {
                    logger.error("can not find consumer list by topic and app refer.", e);
                    topicUnsubscribedApp.setSubscribeGroupExist(true);
                }
            }
            return topicUnsubscribedApp;
        }).collect(Collectors.toList()));
    }

    private List<String> getSubscribeList(QApplication query){
        try{
            List<String> noInCodes = null;
            if (query.getSubscribeType() != null) {
                if (query.getSubscribeType() == Producer.PRODUCER_TYPE) {
                    if (query.getTopic() == null ) {
                        throw new RuntimeException("topic is null");
                    }
                    List<Producer> producerList = producerNameServerService.findByTopic(query.getTopic().getCode(), query.getTopic().getNamespace().getCode());
                    if (producerList == null)return null;
                    noInCodes = producerList.stream().map(producer -> producer.getApp().getCode()).collect(Collectors.toList());
                }
//                if (query.getSubscribeType() == Consumer.CONSUMER_TYPE) {
////                    QConsumer qConsumer = new QConsumer();
////                    qConsumer.setTopic(query.getTopic());
////                    List<Consumer> consumerList = consumerNameServerService.findByQuery(qConsumer);
////                    if (consumerList == null)return null;
////                    noInCodes = consumerList.stream().map(producer -> producer.getApp().getCode()).collect(Collectors.toList());
////                }
            }
            return noInCodes;
        } catch (Exception e) {
            logger.error("getNoInCodes",e);
        }
        return null;
    }

    @Override
    public List<Application> findByCodes(List<String> codes) {
        return  repository.findByCodes(codes);
    }
}
