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
package io.chubao.joyqueue.handler.routing.command.monitor;

import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Consumer;
import io.chubao.joyqueue.model.domain.ConsumerConfig;
import io.chubao.joyqueue.model.query.QConsumer;
import io.chubao.joyqueue.nsr.ConsumerNameServerService;
import io.chubao.joyqueue.service.ApplicationService;
import io.chubao.joyqueue.service.ConsumerService;
import io.chubao.joyqueue.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static io.chubao.joyqueue.handler.Constants.ID;
import static io.chubao.joyqueue.handler.Constants.TOPIC;


public class ConsumerCommand extends NsrCommandSupport<Consumer, ConsumerService, QConsumer> {
    private final Logger logger = LoggerFactory.getLogger(ConsumerCommand.class);
    @Value(nullable = false)
    private ApplicationService applicationService;
    @Value(nullable = false)
    private TopicService topicService;
    @Value(nullable = false)
    private ConsumerNameServerService consumerNameServerService;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QConsumer> qPageQuery) throws Exception {
        QConsumer query = qPageQuery.getQuery();
        List<Consumer> consumers = Collections.emptyList();

        if (query.getApp() != null) {
            consumers = service.findByApp(query.getApp().getCode());
        } else if (query.getTopic() != null) {
            consumers = service.findByTopic(query.getTopic().getCode(), query.getTopic().getNamespace().getCode());
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
    public Response delete(@QueryParam(ID) String id) throws Exception {
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
    public Response findAppsByTopic(@QueryParam(TOPIC)String topic) throws Exception {
        return Responses.success(service.findAppsByTopic(topic));
    }

}
