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
package io.chubao.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.chubao.joyqueue.convert.CodeConverter;
import io.chubao.joyqueue.convert.NsrConsumerConverter;
import io.chubao.joyqueue.domain.ClientType;
import io.chubao.joyqueue.model.domain.Consumer;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QConsumer;
import io.chubao.joyqueue.nsr.ConsumerNameServerService;
import io.chubao.joyqueue.nsr.NameServerBase;
import io.chubao.joyqueue.nsr.model.ConsumerQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("consumerNameServerService")
public class ConsumerNameServerServiceImpl extends NameServerBase implements ConsumerNameServerService {
    public static final String ADD_CONSUMER="/consumer/add";
    public static final String UPDATE_CONSUMER="/consumer/update";
    public static final String REMOVE_CONSUMER="/consumer/remove";
    public static final String GETALL_CONSUMER="/consumer/list";
    public static final String GETTOPICAPP_CONSUMER="/consumer/getByTopicAndApp";
    public static final String GETBYAPP_CONSUMER="/consumer/getByApp";
    public static final String GETBYTOPIC_CONSUMER="/consumer/getByTopic";
    public static final String GETBYID_CONSUMER="/consumer/getById";

    private NsrConsumerConverter nsrConsumerConverter = new NsrConsumerConverter();

    /**
     * 添加consumer
     * @param consumer
     * @throws Exception
     */
    public int add(Consumer consumer) throws Exception {
        io.chubao.joyqueue.domain.Consumer nsrConsumer = nsrConsumerConverter.convert(consumer);
        String result = postWithLog(ADD_CONSUMER, nsrConsumer, OperLog.Type.CONSUMER.value(),OperLog.OperType.ADD.value(),consumer.getTopic().getCode());
        return isSuccess(result);
    }

    /**
     * 更新consumer
     * @param consumer
     * @throws Exception
     */
    public int update(Consumer consumer) throws Exception {
        io.chubao.joyqueue.domain.Consumer nsrConsumer = nsrConsumerConverter.convert(consumer);
        String result1 = postWithLog(UPDATE_CONSUMER, nsrConsumer,OperLog.Type.CONSUMER.value(),OperLog.OperType.UPDATE.value(),consumer.getTopic().getCode());
        return isSuccess(result1);
    }


    /**
     * 删除consumer
     *
     * @param consumer
     * @throws Exception
     */
    public int delete(Consumer consumer) throws Exception {
        io.chubao.joyqueue.domain.Consumer nsrConsumer = new io.chubao.joyqueue.domain.Consumer();
        nsrConsumer.setApp(CodeConverter.convertApp(consumer.getApp(), consumer.getSubscribeGroup()));
        nsrConsumer.setClientType(ClientType.valueOf(consumer.getClientType()));
        nsrConsumer.setTopic(CodeConverter.convertTopic(consumer.getNamespace(),consumer.getTopic()));
        String result = postWithLog(REMOVE_CONSUMER, nsrConsumer,OperLog.Type.CONSUMER.value(),OperLog.OperType.DELETE.value(),consumer.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public Consumer findById(String  id) throws Exception {
        String result = post(GETBYID_CONSUMER,id);
        io.chubao.joyqueue.domain.Consumer nsrConsumer = JSONObject.parseObject(result, io.chubao.joyqueue.domain.Consumer.class);
        return nsrConsumerConverter.revert(nsrConsumer);
    }

    @Override
    public List<Consumer> findAll() throws Exception {
        String result = post(GETALL_CONSUMER, null);
        List<io.chubao.joyqueue.domain.Consumer> consumerList = JSON.parseArray(result).toJavaList(io.chubao.joyqueue.domain.Consumer.class);
        return consumerList.stream().map(consumer -> nsrConsumerConverter.revert(consumer)).collect(Collectors.toList());
    }

    @Override
    public Consumer findByTopicAndApp(String topic, String namespace, String app) throws Exception {
        ConsumerQuery consumerQuery = new ConsumerQuery();
        consumerQuery.setTopic(topic);
        consumerQuery.setNamespace(namespace);
        consumerQuery.setApp(app);

        String result = post(GETTOPICAPP_CONSUMER, consumerQuery);
        io.chubao.joyqueue.domain.Consumer consumer = JSON.parseObject(result, io.chubao.joyqueue.domain.Consumer.class);
        return nsrConsumerConverter.revert(consumer);
    }

    @Override
    public List<Consumer> findByApp(String app) throws Exception {
        ConsumerQuery consumerQuery = new ConsumerQuery();
        consumerQuery.setApp(app);

        String result = post(GETBYAPP_CONSUMER, consumerQuery);
        List<io.chubao.joyqueue.domain.Consumer> consumerList = JSON.parseArray(result).toJavaList(io.chubao.joyqueue.domain.Consumer.class);
        return consumerList.stream().map(consumer -> nsrConsumerConverter.revert(consumer)).collect(Collectors.toList());
    }

    @Override
    public List<Consumer> findByTopic(String topic, String namespace) throws Exception {
        ConsumerQuery consumerQuery = new ConsumerQuery();
        consumerQuery.setTopic(topic);
        consumerQuery.setNamespace(namespace);

        String result = post(GETBYTOPIC_CONSUMER, consumerQuery);
        List<io.chubao.joyqueue.domain.Consumer> consumerList = JSON.parseArray(result).toJavaList(io.chubao.joyqueue.domain.Consumer.class);
        return consumerList.stream().map(consumer -> nsrConsumerConverter.revert(consumer)).collect(Collectors.toList());
    }

    @Override
    public List<String> findAllSubscribeGroups() throws Exception {
        //todo 不要查全部consumer
        List<Consumer> consumerList = findAll();
        if (consumerList != null && consumerList.size() > 0) {
            return consumerList.stream().map(consumer -> consumer.getSubscribeGroup())
                    .filter(sg -> StringUtils.isNotBlank(sg)).collect(Collectors.toList());
        }
        return null;
    }

    private ConsumerQuery qConsumerConvert(QConsumer query){
        ConsumerQuery consumerQuery = new ConsumerQuery();
        if (query != null) {
            if (query.getTopic() != null) {
                consumerQuery.setTopic(query.getTopic().getCode());
                if(query.getKeyword() != null ) {
                    consumerQuery.setApp(query.getKeyword());
                }
            }
            if (query.getApp() != null) {
                consumerQuery.setApp(query.getApp().getCode());
            }
            if (query.getAppList() != null) {
                consumerQuery.setAppList(query.getAppList());
            }
            if (query.getReferer() != null) {
                consumerQuery.setReferer(query.getReferer());
                if(query.getKeyword() != null ) {
                    consumerQuery.setTopic(query.getKeyword());
                }
            }
            if (query.getClientType() != -1) {
                consumerQuery.setClientType(query.getClientType());
            }
            if (query.getNamespace() != null) {
                consumerQuery.setNamespace(query.getNamespace());
            }
        }
        return consumerQuery;
    }
}
