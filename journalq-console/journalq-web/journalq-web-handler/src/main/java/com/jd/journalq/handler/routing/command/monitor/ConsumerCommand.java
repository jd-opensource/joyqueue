package com.jd.journalq.handler.routing.command.monitor;

import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.handler.binder.BodyType;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.binder.annotation.Body;
import com.jd.journalq.handler.binder.annotation.GenericBody;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.service.ApplicationService;
import com.jd.journalq.service.ConsumerService;
import com.jd.journalq.service.TopicService;
import com.jd.journalq.nsr.ConsumerNameServerService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConsumerCommand extends NsrCommandSupport<Consumer, ConsumerService, QConsumer> {
    private final Logger logger = LoggerFactory.getLogger(ConsumerCommand.class);
    @Value(nullable = false)
    private ApplicationService applicationService;
    @Value(nullable = false)
    private TopicService topicService;
    @Value(nullable = false)
    private ConsumerNameServerService consumerNameServerService;

    @Path("add")
    public Response add(@GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0) Consumer consumer) throws Exception {
        //validate unique
        List<Consumer> consumers = service.findByQuery(new QConsumer(consumer.getTopic().getCode(),
                consumer.getTopic().getNamespace().getCode(), AppName.parse(consumer.getApp().getCode(), consumer.getSubscribeGroup()).getFullName()));
        if (consumers != null && consumers.size() > 0) {
            throw new ConfigException(ErrorCode.BadRequest, "consumer already exists!");
        }
        return super.add(consumer);
    }

    @Override
    @Path("delete")
    public Response delete(@ParamterValue(Constants.ID) Object id) throws Exception {
        String consumerId = id.toString();
        Consumer consumer = service.findById(consumerId);
        int count = service.delete(consumer);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //afterDelete(model);
        return Responses.success();
    }

    @Path("configAddOrUpdate")
    public Response configAddOrUpdate(@Body(typeindex = 0,type = BodyType.JSON) ConsumerConfig config) throws Exception {
        if(config!=null){
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
    @Path("syncMqttClient")
    public Response syncMqttConsumers() throws Exception{
        int successCount = 0;
        int failCount = 0;
        List<Consumer> consumerList = consumerNameServerService.syncConsumer(ClientType.MQTT.value());
        Map<String,Topic> topicMap = new HashMap<>();
        Map<String, Application> appMap = new HashMap<>();
        for(Consumer consumer : consumerList){
            try {
                Topic topic = topicMap.get(consumer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + consumer.getTopic().getCode());
                if (null == topic) {
                    topic = topicService.findByCode(consumer.getNamespace().getCode(), consumer.getTopic().getCode());
                    if(null==topic){
                        logger.error("namespace {} topic {} 不存在",consumer.getNamespace().getCode(),consumer.getTopic().getCode());
                        failCount++;
                        continue;
                    }
                    topicMap.put(consumer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + consumer.getTopic().getCode(), topic);
                }
                consumer.getTopic().setId(topic.getId());
                consumer.getNamespace().setId(topic.getNamespace().getId());
                Application application = appMap.get(consumer.getApp().getCode());
                if (null == application) {
                    application = applicationService.findByCode(consumer.getApp().getCode());
                    if(null==application){
                        logger.error("application {} 不存在",consumer.getApp().getCode());
                        failCount++;
                        continue;
                    }
                    appMap.put(application.getCode(), application);
                }
                consumer.getApp().setId(application.getId());
                Consumer consumerExist = service.findByTopicAppGroup(consumer.getNamespace().getCode(), consumer.getTopic().getCode(), consumer.getApp().getCode(), consumer.getSubscribeGroup());
                if (null != consumerExist) {
                    consumer.setId(consumerExist.getId());
//                    consumer.setCreateBy(consumerExist.getCreateBy());
//                    consumer.setUpdateBy(new Identity(session));
                    service.update(consumer);
                } else {
//                    consumer.setCreateBy(new Identity(session));
//                    consumer.setUpdateBy(consumer.getCreateBy());
                    service.add(consumer);
                }
                successCount++;
            }catch (Exception e){
                failCount++;
                logger.error("同步consumer[{}]异常",consumer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + consumer.getTopic().getCode()+TopicName.TOPIC_SEPARATOR+consumer.getApp()+TopicName.TOPIC_SEPARATOR+consumer.getSubscribeGroup(),e);
            }
        }
        return Responses.success("同步mqtt consumer成功"+successCount+"条,失败"+failCount+"条");
    }

    @Path("findAllSubscribeGroups")
    public Response findAllSubscribeGroups() throws Exception {
        return Responses.success(service.findAllSubscribeGroups());
    }

}
