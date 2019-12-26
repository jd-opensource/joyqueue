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
package org.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.joyqueue.convert.CodeConverter;
import org.joyqueue.convert.NsrProducerConverter;
import org.joyqueue.domain.ClientType;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.model.domain.Producer;
import org.joyqueue.model.query.QProducer;
import org.joyqueue.nsr.NameServerBase;
import org.joyqueue.nsr.ProducerNameServerService;
import org.joyqueue.nsr.model.ProducerQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("producerNameServerService")
public class ProducerNameServerServiceImpl extends NameServerBase implements ProducerNameServerService {

    public static final String ADD_PRODUCER="/producer/add";
    public static final String UPDATE_PRODUCER="/producer/update";
    public static final String REMOVE_PRODUCER="/producer/remove";
    public static final String GETBYID_PRODUCER="/producer/getById";
    public static final String GETBYTOPIC_PRODUCER="/producer/getByTopic";
    public static final String GETBYTOPICANDAPP_PRODUCER="/producer/getByTopicAndApp";
    public static final String GETBYAPP_PRODUCER="/producer/getByApp";

    private NsrProducerConverter nsrProducerConverter = new NsrProducerConverter();

    /**
     * 添加producer
     * @param producer
     * @throws Exception
     */
    @Override
    public int add(Producer producer) throws Exception {
        org.joyqueue.domain.Producer nsrProducer = nsrProducerConverter.convert(producer);
        String result = postWithLog(ADD_PRODUCER, nsrProducer, OperLog.Type.PRODUCER.value(),OperLog.OperType.ADD.value(),producer.getTopic().getCode());
        return isSuccess(result);
    }

    /**
     * 更新producer
     * @param producer
     * @throws Exception
     */
    @Override
    public int update(Producer producer) throws Exception {
        org.joyqueue.domain.Producer nsrProducer = nsrProducerConverter.convert(producer);
        String result1 = postWithLog(UPDATE_PRODUCER, nsrProducer,OperLog.Type.PRODUCER.value(),OperLog.OperType.UPDATE.value(),producer.getTopic().getCode());
        return isSuccess(result1);
    }

    /**
     * 删除producer
     * @param producer
     * @throws Exception
     */
    @Override
    public int delete(Producer producer) throws Exception {
        org.joyqueue.domain.Producer nsrProducer = new org.joyqueue.domain.Producer();
        nsrProducer.setApp(producer.getApp().getCode());
        nsrProducer.setClientType(ClientType.valueOf(producer.getClientType()));
        nsrProducer.setTopic(CodeConverter.convertTopic(producer.getNamespace(),producer.getTopic()));
        String result = postWithLog(REMOVE_PRODUCER, nsrProducer,OperLog.Type.PRODUCER.value(),OperLog.OperType.DELETE.value(),producer.getTopic().getCode());
        return isSuccess(result);
    }

//    @Override
//    public List<Producer> syncProducer(byte clientType) throws Exception {
//        JSONObject request = new JSONObject();
//        request.put("client_type",clientType);
//        List<org.joyqueue.domain.Producer>  nsrProducers = JSONArray.parseArray(post(PRODUCER_GETALL_BY_CLIENTTYPE,request), org.joyqueue.domain.Producer.class);
//        List<Producer> producerList = new ArrayList<>(nsrProducers.size());
//        nsrProducers.forEach(nsrProducer->{
//            Producer producer = new Producer();
//            TopicName nt = nsrProducer.getTopic();
//            producer.setApp(new Identity(null,nsrProducer.getApp()));
//            if(nt.getNamespace().equals(TopicName.DEFAULT_NAMESPACE)){
//                producer.setNamespace(new Namespace(DEFAULT_NAMESPACE_ID, DEFAULT_NAMESPACE_CODE));
//            }else{
//                producer.setNamespace(new Namespace(nt.getNamespace()));
//            }
//            producer.setTopic(new Topic(null,EscapeUtils.reEscapeTopic(nt.getCode())));
//            //producer.setSubscribeGroup(ag[1]);
//            producer.setClientType(nsrProducer.getClientType().value());
//            producerList.add(producer);
//        });
//        return producerList;
//    }


    @Override
    public List<Producer> findByApp(String app) throws Exception {
        ProducerQuery producerQuery = new ProducerQuery();
        producerQuery.setApp(app);
        String result = post(GETBYAPP_PRODUCER,producerQuery);
        List<org.joyqueue.domain.Producer> producerList = JSON.parseArray(result).toJavaList(org.joyqueue.domain.Producer.class);
        return producerList.stream().map(producer -> nsrProducerConverter.revert(producer)).collect(Collectors.toList());
    }

    @Override
    public List<Producer> findByTopic(String topic, String namespace) throws Exception {
        ProducerQuery producerQuery = new ProducerQuery();
        producerQuery.setTopic(topic);
        producerQuery.setNamespace(namespace);
        String result = post(GETBYTOPIC_PRODUCER,producerQuery);
        List<org.joyqueue.domain.Producer> producerList = JSON.parseArray(result).toJavaList(org.joyqueue.domain.Producer.class);
        return producerList.stream().map(producer -> nsrProducerConverter.revert(producer)).collect(Collectors.toList());
    }

    @Override
    public Producer findById(String nsrProducerId) throws Exception {
        String result = post(GETBYID_PRODUCER,nsrProducerId);
        org.joyqueue.domain.Producer nsrProducer = JSONObject.parseObject(result, org.joyqueue.domain.Producer.class);
        return nsrProducerConverter.revert(nsrProducer);
    }

    @Override
    public Producer findByTopicAppGroup(String namespace, String topic, String app) throws Exception {
        ProducerQuery producerQuery = new ProducerQuery();
        producerQuery.setTopic(topic);
        producerQuery.setNamespace(namespace);
        producerQuery.setApp(app);
        String result = post(GETBYTOPICANDAPP_PRODUCER,producerQuery);
        org.joyqueue.domain.Producer producer = JSON.parseObject(result, org.joyqueue.domain.Producer.class);
        return nsrProducerConverter.revert(producer);
    }
    private ProducerQuery producerQueryConvert(QProducer query){
        ProducerQuery producerQuery = new ProducerQuery();
        if (query != null) {
            if (query.getApp() != null) {
                producerQuery.setApp(query.getApp().getCode());
                if(query.getKeyword() != null ) {
                    producerQuery.setTopic(query.getKeyword());
                }
            }
            if (query.getTopic() != null) {
                producerQuery.setTopic(query.getTopic().getCode());
                if(query.getKeyword() != null ) {
                    producerQuery.setApp(query.getKeyword());
                }
            }
            if (query.getTopic() != null && query.getTopic().getNamespace() != null) {
                producerQuery.setNamespace(query.getTopic().getNamespace().getCode());
            }
            if (query.getAppList() != null) {
                producerQuery.setAppList(query.getAppList());
            }
        }
        return producerQuery;
    }
}
