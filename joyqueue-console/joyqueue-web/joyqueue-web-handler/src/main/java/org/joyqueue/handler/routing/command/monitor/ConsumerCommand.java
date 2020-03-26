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
package org.joyqueue.handler.routing.command.monitor;

import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Consumer;
import org.joyqueue.model.domain.ConsumerConfig;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QConsumer;
import org.joyqueue.nsr.ConsumerNameServerService;
import org.joyqueue.service.ApplicationService;
import org.joyqueue.service.ApplicationUserService;
import org.joyqueue.service.ConsumerService;
import org.joyqueue.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ConsumerCommand extends NsrCommandSupport<Consumer, ConsumerService, QConsumer> {
    private final Logger logger = LoggerFactory.getLogger(ConsumerCommand.class);
    @Value(nullable = false)
    private ApplicationService applicationService;
    @Value(nullable = false)
    private TopicService topicService;
    @Value(nullable = false)
    private ConsumerNameServerService consumerNameServerService;
    @Value(nullable = false)
    private ApplicationUserService applicationUserService;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QConsumer> qPageQuery) throws Exception {
        QConsumer query = qPageQuery.getQuery();
        List<Consumer> consumers = new ArrayList<>(0);

        if (query.getApp() != null && query.getTopic() ==null) {
            consumers = service.findByFuzzyTopicAndApp("%"+query.getKeyword()+"%",query.getApp().getCode());
        } else if (query.getTopic() != null && query.getApp() == null) {
            consumers = service.findByFuzzyTopicAndApp(query.getTopic().getCode(),"%"+query.getKeyword()+"%");
        } else if (query.getApp() !=null && query.getTopic()!=null) {
            consumers.add(service.findByTopicAppGroup(query.getTopic().getNamespace().getCode(),query.getTopic().getCode(),query.getApp().getCode(),null));
        }

        if (CollectionUtils.isNotEmpty(consumers) && session.getRole() != User.UserRole.ADMIN.value()) {
            Iterator<Consumer> iterator = consumers.iterator();
            while (iterator.hasNext()) {
                Consumer consumer = iterator.next();
                if (applicationUserService.findByUserApp(session.getCode(), consumer.getApp().getCode().split("\\.")[0]) == null) {
                    iterator.remove();
                }
            }
        }

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(consumers.size());

        PageResult<Consumer> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(consumers);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("add")
    public Response add(@Body Consumer consumer) throws Exception {
        //validate unique
        Consumer currentConsumer = service.findByTopicAppGroup(consumer.getTopic().getNamespace().getCode(), consumer.getTopic().getCode(), consumer.getApp().getCode(), consumer.getSubscribeGroup());
        if (currentConsumer != null) {
            throw new ConfigException(ErrorCode.BadRequest, "consumer already exists!");
        }
        return super.add(consumer);
    }

    @Override
    @Path("delete")
    public Response delete(@QueryParam(Constants.ID) String id) throws Exception {
        Consumer consumer = service.findById(id);
        int count = service.delete(consumer);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //afterDelete(model);
        return Responses.success();
    }

    @Path("configAddOrUpdate")
    public Response configAddOrUpdate(@Body ConsumerConfig config) throws Exception {
        if (config != null) {
            Consumer consumer = service.findById(config.getConsumerId());
            consumer.setConfig(config);
            service.update(consumer);
        }
        return Responses.success();
    }


    /**
     * 同步producer
     * @return
     * @throws Exception
     */
//    @Path("syncMqttClient")
//    public Response syncMqttConsumers() throws Exception {
//        int successCount = 0;
//        int failCount = 0;
//        List<Consumer> consumerList = consumerNameServerService.syncConsumer(ClientType.MQTT.value());
//        Map<String, Topic> topicMap = new HashMap<>();
//        Map<String, Application> appMap = new HashMap<>();
//        for (Consumer consumer : consumerList) {
//            try {
//                Topic topic = topicMap.get(consumer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + consumer.getTopic().getCode());
//                if (null == topic) {
//                    topic = topicService.findByCode(consumer.getNamespace().getCode(), consumer.getTopic().getCode());
//                    if (null == topic) {
//                        logger.error("namespace {} topic {} 不存在", consumer.getNamespace().getCode(), consumer.getTopic().getCode());
//                        failCount++;
//                        continue;
//                    }
//                    topicMap.put(consumer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + consumer.getTopic().getCode(), topic);
//                }
//                consumer.getTopic().setId(topic.getId());
//                consumer.getNamespace().setId(topic.getNamespace().getId());
//                Application application = appMap.get(consumer.getApp().getCode());
//                if (null == application) {
//                    application = applicationService.findByCode(consumer.getApp().getCode());
//                    if (null == application) {
//                        logger.error("application {} 不存在", consumer.getApp().getCode());
//                        failCount++;
//                        continue;
//                    }
//                    appMap.put(application.getCode(), application);
//                }
//                consumer.getApp().setId(application.getId());
//                Consumer consumerExist = service.findByTopicAppGroup(consumer.getNamespace().getCode(), consumer.getTopic().getCode(), consumer.getApp().getCode(), consumer.getSubscribeGroup());
//                if (null != consumerExist) {
//                    consumer.setId(consumerExist.getId());
////                    consumer.setCreateBy(consumerExist.getCreateBy());
////                    consumer.setUpdateBy(new Identity(session));
//                    service.update(consumer);
//                } else {
////                    consumer.setCreateBy(new Identity(session));
////                    consumer.setUpdateBy(consumer.getCreateBy());
//                    service.add(consumer);
//                }
//                successCount++;
//            } catch (Exception e) {
//                failCount++;
//                logger.error("同步consumer[{}]异常",
//                        consumer.getNamespace().getCode() +
//                                TopicName.TOPIC_SEPARATOR +
//                                consumer.getTopic().getCode() +
//                                TopicName.TOPIC_SEPARATOR +
//                                consumer.getApp() +
//                                TopicName.TOPIC_SEPARATOR + consumer.getSubscribeGroup(), e);
//            }
//        }
//        return Responses.success("同步mqtt consumer成功" + successCount + "条,失败" + failCount + "条");
//    }

    @Path("findAllSubscribeGroups")
    public Response findAllSubscribeGroups() throws Exception {
        return Responses.success(service.findAllSubscribeGroups());
    }

    @Path("findAppsByTopic")
    public Response findAppsByTopic(@QueryParam(Constants.TOPIC)String topic) throws Exception {
        return Responses.success(service.findAppsByTopic(topic));
    }

}
