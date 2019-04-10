package com.jd.journalq.handler.routing.command.monitor;

import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QProducer;
import com.jd.journalq.service.ApplicationService;
import com.jd.journalq.service.ProducerService;
import com.jd.journalq.service.TopicPartitionGroupService;
import com.jd.journalq.service.TopicService;
import com.jd.journalq.nsr.ProducerNameServerService;
import com.jd.journalq.util.NullUtil;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jd.journalq.handler.Constants.ID;


public class ProducerCommand extends NsrCommandSupport<Producer, ProducerService, QProducer> {
    private final Logger logger = LoggerFactory.getLogger(ProducerCommand.class);

    @Value(nullable = false)
    private ApplicationService applicationService;
    @Value(nullable = false)
    private TopicService topicService;
    @Value(nullable = false)
    private TopicPartitionGroupService topicPartitionGroupService;
    @Value(nullable = false)
    protected ProducerNameServerService producerNameServerService;

    @Override
    @Path("delete")
    public Response delete(@QueryParam(ID) String id) throws Exception {
        Producer producer = service.findById(id);
        int count = service.delete(producer);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success();
    }

    @Path("weight")
    public Response findPartitionGroupWeight(@QueryParam(ID) String id) throws Exception {
        Producer producer = service.findById(id);
        List<PartitionGroupWeight> currentWeights=new ArrayList<>();
        if(!NullUtil.isEmpty(producer)) {
          ProducerConfig producerConfig=  producer.getConfig();
          Map<String,Short> weights=producerConfig.weights();
          PartitionGroupWeight weight;
          List<TopicPartitionGroup>  topicPartitionGroups= topicPartitionGroupService.findByTopic(producer.getNamespace(),producer.getTopic());
          for(TopicPartitionGroup p:topicPartitionGroups){
              Short weightVal=0;
              if(!NullUtil.isEmpty(weights)&&weights.get(String.valueOf(p.getGroupNo()))!=null)
                 weightVal=weights.get(String.valueOf(p.getGroupNo()));
              weight=new PartitionGroupWeight();
              weight.setGroupNo(String.valueOf(p.getGroupNo()));
              weight.setWeight(weightVal);
              currentWeights.add(weight);
          }
        }
        return Responses.success(currentWeights);
    }

    /**
     * 同步producer
     * @return
     * @throws Exception
     */
    @Path("syncMqttClient")
    public Response syncMqttProducers() throws Exception{
        int successCount = 0;
        int failCount = 0;
        List<Producer> producerList = producerNameServerService.syncProducer(ClientType.MQTT.value());
        Map<String,Topic> topicMap = new HashMap<>();
        Map<String,Application> appMap = new HashMap<>();
        for(Producer producer : producerList){
            try {
                Topic topic = topicMap.get(producer.getNamespace().getCode() +TopicName.TOPIC_SEPARATOR+ producer.getTopic().getCode());
                if (null == topic) {
                    topic = topicService.findByCode(producer.getNamespace().getCode(), producer.getTopic().getCode());
                    if(null==topic){
                        logger.error("namespace {} topic {} 不存在",producer.getNamespace().getCode(),producer.getTopic().getCode());
                        failCount++;
                        continue;
                    }
                    topicMap.put(producer.getNamespace().getCode() +TopicName.TOPIC_SEPARATOR + producer.getTopic().getCode(), topic);
                }
                producer.getTopic().setId(topic.getId());
                producer.getNamespace().setId(topic.getNamespace().getId());
                Application application = appMap.get(producer.getApp().getCode());
                if (null == application) {
                    application = applicationService.findByCode(producer.getApp().getCode());
                    if(null==application){
                        logger.error("application {} 不存在",producer.getApp().getCode());
                        failCount++;
                        continue;
                    }
                    appMap.put(application.getCode(), application);
                }
                producer.getApp().setId(application.getId());
                Producer producerExist = service.findByTopicAppGroup(producer.getNamespace().getCode(), producer.getTopic().getCode(), producer.getApp().getCode());
                if (null != producerExist) {
                    producer.setId(producerExist.getId());
                    service.update(producer);
                } else {
                    service.add(producer);
                }
                successCount++;
            }catch (Exception e){
                failCount++;
                logger.error("同步producer[{}]异常",producer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + producer.getTopic().getCode()+TopicName.TOPIC_SEPARATOR+producer.getApp(),e);
            }
        }
        return Responses.success("同步mqtt producer成功"+successCount+"条,失败"+failCount+"条");
    }



}
