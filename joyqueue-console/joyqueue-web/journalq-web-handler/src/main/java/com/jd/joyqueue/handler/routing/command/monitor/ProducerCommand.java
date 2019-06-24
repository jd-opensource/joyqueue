/**
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
package com.jd.joyqueue.handler.routing.command.monitor;

import com.jd.joyqueue.domain.ClientType;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.handler.error.ConfigException;
import com.jd.joyqueue.handler.routing.command.NsrCommandSupport;
import com.jd.joyqueue.model.domain.Application;
import com.jd.joyqueue.model.domain.PartitionGroupWeight;
import com.jd.joyqueue.model.domain.Producer;
import com.jd.joyqueue.model.domain.ProducerConfig;
import com.jd.joyqueue.model.domain.Topic;
import com.jd.joyqueue.model.domain.TopicPartitionGroup;
import com.jd.joyqueue.model.query.QProducer;
import com.jd.joyqueue.service.ApplicationService;
import com.jd.joyqueue.service.ProducerService;
import com.jd.joyqueue.service.TopicPartitionGroupService;
import com.jd.joyqueue.service.TopicService;
import com.jd.joyqueue.nsr.ProducerNameServerService;
import com.jd.joyqueue.util.NullUtil;
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

import static com.jd.joyqueue.handler.Constants.ID;


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
