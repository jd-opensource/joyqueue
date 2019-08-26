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
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.PartitionGroupWeight;
import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.domain.ProducerConfig;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QProducer;
import io.chubao.joyqueue.nsr.ProducerNameServerService;
import io.chubao.joyqueue.service.ApplicationService;
import io.chubao.joyqueue.service.ProducerService;
import io.chubao.joyqueue.service.TopicPartitionGroupService;
import io.chubao.joyqueue.service.TopicService;
import io.chubao.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.chubao.joyqueue.handler.Constants.ID;


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

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QProducer> qPageQuery) throws Exception {
        QProducer query = qPageQuery.getQuery();
        List<Producer> producers = Collections.emptyList();

        if (query.getApp() != null) {
            producers = service.findByApp(query.getApp().getCode());
        } else if (query.getTopic() != null) {
            producers = service.findByTopic(query.getTopic().getNamespace().getCode(), query.getTopic().getCode());
        }

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(producers.size());

        PageResult<Producer> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(producers);
        return Responses.success(result.getPagination(), result.getResult());
    }

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
//    @Path("syncMqttClient")
//    public Response syncMqttProducers() throws Exception{
//        int successCount = 0;
//        int failCount = 0;
//        List<Producer> producerList = producerNameServerService.syncProducer(ClientType.MQTT.value());
//        Map<String,Topic> topicMap = new HashMap<>();
//        Map<String,Application> appMap = new HashMap<>();
//        for(Producer producer : producerList){
//            try {
//                Topic topic = topicMap.get(producer.getNamespace().getCode() +TopicName.TOPIC_SEPARATOR+ producer.getTopic().getCode());
//                if (null == topic) {
//                    topic = topicService.findByCode(producer.getNamespace().getCode(), producer.getTopic().getCode());
//                    if(null==topic){
//                        logger.error("namespace {} topic {} 不存在",producer.getNamespace().getCode(),producer.getTopic().getCode());
//                        failCount++;
//                        continue;
//                    }
//                    topicMap.put(producer.getNamespace().getCode() +TopicName.TOPIC_SEPARATOR + producer.getTopic().getCode(), topic);
//                }
//                producer.getTopic().setId(topic.getId());
//                producer.getNamespace().setId(topic.getNamespace().getId());
//                Application application = appMap.get(producer.getApp().getCode());
//                if (null == application) {
//                    application = applicationService.findByCode(producer.getApp().getCode());
//                    if(null==application){
//                        logger.error("application {} 不存在",producer.getApp().getCode());
//                        failCount++;
//                        continue;
//                    }
//                    appMap.put(application.getCode(), application);
//                }
//                producer.getApp().setId(application.getId());
//                Producer producerExist = service.findByTopicAppGroup(producer.getNamespace().getCode(), producer.getTopic().getCode(), producer.getApp().getCode());
//                if (null != producerExist) {
//                    producer.setId(producerExist.getId());
//                    service.update(producer);
//                } else {
//                    service.add(producer);
//                }
//                successCount++;
//            }catch (Exception e){
//                failCount++;
//                logger.error("同步producer[{}]异常",producer.getNamespace().getCode() + TopicName.TOPIC_SEPARATOR + producer.getTopic().getCode()+TopicName.TOPIC_SEPARATOR+producer.getApp(),e);
//            }
//        }
//        return Responses.success("同步mqtt producer成功"+successCount+"条,失败"+failCount+"条");
//    }



}
