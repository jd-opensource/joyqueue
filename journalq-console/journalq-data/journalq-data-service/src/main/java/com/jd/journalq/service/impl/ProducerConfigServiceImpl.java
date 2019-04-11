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
//package com.jd.journalq.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import BaseModel;
//import Producer;
//import ProducerConfig;
//import com.jd.journalq.repository.ProducerConfigRepository;
//import com.jd.journalq.repository.ProducerRepository;
//import com.jd.journalq.service.ProducerConfigService;
//import ProducerNameServerService;
//import Preconditions;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//
//@Service("producerConfigService")
//public class ProducerConfigServiceImpl extends ServiceSupport<ProducerConfig, ProducerConfigRepository> implements ProducerConfigService {
//    private final Logger logger = LoggerFactory.getLogger(ProducerConfigServiceImpl.class);
//
//    @Autowired
//    private ProducerNameServerService producerNameServerService;
//
//    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
//    @Override
//    public int stateByProducer(Producer producer) {
//        //Validate
//        Preconditions.checkArgument(producer != null && producer.getId()>0 && producer.getUpdateBy() != null, "invalidate producer arg.");
//        if (producer.getUpdateTime() == null) {
//            producer.setUpdateTime(new Date());
//        }
//
//        int count;
//        try {
//            //Update producer config status
//            count = repository.stateByProducer(producer);
//            if (count != 1) {
//                throw new IllegalStateException("state producer error.");
//            }
//            //Find producer config
//            ProducerConfig config = findByProducerId(producer.getId());
//            //Refind producer to avoid some fields loss
//            Producer newProducer = producerNameServerService.findById(producer.getIgniteId());
//            newProducer.setConfig(config);
//            //Sync nameServer
//            if (producer.getStatus()==BaseModel.DELETED) {
//                producerNameServerService.delete(newProducer);
//            } else {
//                producerNameServerService.update(newProducer);
//            }
//        }catch (Exception e) {
//            String errorMsg = String.format("update producer config status with nameServer by producer failed, producer id is %s.", producer.getId());
//            logger.error(errorMsg, e);
//            throw new RuntimeException(errorMsg, e);
//        }
//        return count;
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
//    public ProducerConfig addOrUpdate(ProducerConfig config) {
//        //Validate
//        Preconditions.checkArgument(config!=null && config.getUpdateBy() != null, "invalid producer config arg");
//
//        try {
//            //Find producer
//            Producer producer = producerNameServerService.findById(config.getProducerIgniteId());
//            //Whether exists
//            ProducerConfig existObj = repository.exists(config);
//            if (existObj != null) {// eixsts
//                //Update producer config
//                update(config);
//                //Sync nameServer
//                producer.setConfig(config);
//                producerNameServerService.update(producer);
//            } else {// not exists
//                //Add producer config
//                config.setCreateBy(config.getUpdateBy());
//                add(config);
//                //Sync nameServer
//                producer.setConfig(config);
//                producerNameServerService.add(producer);
//            }
//        }catch (Exception e) {
//            String errorMsg = String.format("add or update producer config with nameServer failed, producer config is %s, exception message is %s.", JSON.toJSONString(config), e.getMessage());
//            logger.error(errorMsg, e);
//            throw new RuntimeException(errorMsg, e);
//        }
//        return config;
//    }
//
//    @Override
//    public ProducerConfig findByProducerId(long producerId) {
//        return repository.findByProducerId(producerId);
//    }
//
//}
