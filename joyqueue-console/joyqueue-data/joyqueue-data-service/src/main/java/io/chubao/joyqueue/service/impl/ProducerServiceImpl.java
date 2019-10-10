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
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.nsr.ProducerNameServerService;
import io.chubao.joyqueue.nsr.TopicNameServerService;
import io.chubao.joyqueue.service.ApplicationService;
import io.chubao.joyqueue.service.ProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("producerService")
public class ProducerServiceImpl  implements ProducerService {
    private final Logger logger = LoggerFactory.getLogger(ProducerServiceImpl.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private TopicNameServerService topicNameServerService;


    @Autowired
    private ProducerNameServerService producerNameServerService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int add(Producer producer) {
        Preconditions.checkArgument(producer!=null && producer.getTopic()!=null, "invalid producer arg");

        int count;
        try {
            //Find topic
            Topic topic = topicNameServerService.findById(producer.getTopic().getId());
            producer.setTopic(topic);
            producer.setNamespace(topic.getNamespace());
            //Add producer
            count = producerNameServerService.add(producer);
            if (count != 1) {
                throw new IllegalStateException("add producer error.");
            }
        }catch (Exception e){
            String errorMsg = String.format("add producer with nameServer failed, producer is %s.", JSON.toJSONString(producer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }

        return count;
    }

    @Override
    public Producer findById(String s) throws Exception {
        return producerNameServerService.findById(s);
    }

    @Override
    public int delete(Producer producer) {
        //Validate
        checkArgument(producer);
        //Update producer status
        int count;
        try {
            count = producerNameServerService.delete(producer);
            if (count != 1) {
                throw new IllegalStateException("update producer status error.");
            }
        }catch (Exception e){
            String errorMsg = String.format("remove producer status by nameServer failed, producer is %s.", JSON.toJSONString(producer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return count;
    }


    @Override
    public int update(Producer producer) {
        //Validate
        checkArgument(producer);
        int count;
        try {
            //Update
            count = producerNameServerService.update(producer);
            if (count != 1) {
                throw new IllegalStateException("update producer error.");
            }
        }catch (Exception e){
            String errorMsg = String.format("update producer by nameServer failed, producer is %s.", JSON.toJSONString(producer));
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);//回滚
        }
        return count;
    }

    @Override
    public List<Producer> findByTopic(String namespace, String topic) {
        try {
            return producerNameServerService.findByTopic(topic, namespace);
        } catch (Exception e) {
            logger.error("findByTopic producer with nameServer failed, producer is {}, {}", namespace, topic, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Producer> findByApp(String app) {
        try {
            return producerNameServerService.findByApp(app);
        } catch (Exception e) {
            logger.error("findByApp producer with nameServer failed, producer is {}", app, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Producer findByTopicAppGroup(String namespace, String topic, String app) {
        try {
            TopicName topicName = TopicName.parse(topic);
            return producerNameServerService.findByTopicAppGroup(namespace, topicName.getCode(), app);
        } catch (Exception e) {
            logger.error("findByTopicAppGroup producer with nameServer failed, producer is {}, {}, {}", namespace, topic, app, e);
            throw new RuntimeException(e);
        }
    }

    private void checkArgument(Producer producer) {
        Preconditions.checkArgument(producer != null, "invalidate producer arg.");
    }

}

