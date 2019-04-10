package com.jd.journalq.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.convert.CodeConverter;
import com.jd.journalq.convert.NsrConsumerConverter;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.nsr.model.ConsumerQuery;
import com.jd.journalq.nsr.ConsumerNameServerService;
import com.jd.journalq.nsr.NameServerBase;
import com.jd.journalq.toolkit.security.EscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jd.journalq.model.domain.Namespace.DEFAULT_NAMESPACE_CODE;
import static com.jd.journalq.model.domain.Namespace.DEFAULT_NAMESPACE_ID;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("consumerNameServerService")
public class ConsumerNameServerServiceImpl extends NameServerBase implements ConsumerNameServerService {
    public static final String ADD_CONSUMER="/consumer/add";
    public static final String UPDATE_CONSUMER="/consumer/update";
    public static final String REMOVE_CONSUMER="/consumer/remove";
    public static final String CONSUMER_GETALL_BY_CLIENTTYPE="/consumer/list";
    public static final String LIST_CONSUMER="/consumer/getList";
    public static final String GETBYID_CONSUMER="/consumer/getById";
    public static final String FINDBYQUERY_CONSUMER="/consumer/findByQuery";

    private NsrConsumerConverter nsrConsumerConverter = new NsrConsumerConverter();

    /**
     * 添加consumer
     * @param consumer
     * @throws Exception
     */
    public int add(Consumer consumer) throws Exception {
        com.jd.journalq.domain.Consumer nsrConsumer = nsrConsumerConverter.convert(consumer);
        String result = postWithLog(ADD_CONSUMER, nsrConsumer, OperLog.Type.CONSUMER.value(),OperLog.OperType.ADD.value(),consumer.getTopic().getCode());
        return isSuccess(result);
    }

    /**
     * 更新consumer
     * @param consumer
     * @throws Exception
     */
    public int update(Consumer consumer) throws Exception {
        com.jd.journalq.domain.Consumer nsrConsumer = nsrConsumerConverter.convert(consumer);
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
        com.jd.journalq.domain.Consumer nsrConsumer = new com.jd.journalq.domain.Consumer();
        nsrConsumer.setApp(CodeConverter.convertApp(consumer.getApp(), consumer.getSubscribeGroup()));
        nsrConsumer.setClientType(ClientType.valueOf(consumer.getClientType()));
        nsrConsumer.setTopic(CodeConverter.convertTopic(consumer.getNamespace(),consumer.getTopic()));
        String result = postWithLog(REMOVE_CONSUMER, nsrConsumer,OperLog.Type.CONSUMER.value(),OperLog.OperType.DELETE.value(),consumer.getTopic().getCode());
        return isSuccess(result);
    }

    public List<Consumer> syncConsumer(byte clientType) throws Exception{
        JSONObject request = new JSONObject();
        request.put("client_type",clientType);
        List<com.jd.journalq.domain.Consumer>  nsrConsumers = JSONArray.parseArray(post(CONSUMER_GETALL_BY_CLIENTTYPE,request), com.jd.journalq.domain.Consumer.class);
        List<Consumer> consumerList = new ArrayList<>(nsrConsumers.size());
        nsrConsumers.forEach(nsrConsumer->{
            Consumer consumer = new Consumer();
            TopicName nt = nsrConsumer.getTopic();
            String[] ag = nsrConsumer.getApp().split(TopicName.TOPIC_SEPARATOR_SPLIT);
            consumer.setApp(new Identity(null,ag[0]));
            if(nt.getNamespace().equals(TopicName.DEFAULT_NAMESPACE)){
                consumer.setNamespace(new Namespace(DEFAULT_NAMESPACE_ID, DEFAULT_NAMESPACE_CODE));
            }else{
                consumer.setNamespace(new Namespace(null,nt.getNamespace()));
            }
            consumer.setTopic(new Topic(null,EscapeUtils.reEscapeTopic(nt.getCode())));
            consumer.setSubscribeGroup(ag[1]);
            consumer.setClientType(nsrConsumer.getClientType().value());
            consumerList.add(consumer);
        });
        return consumerList;
    }
    @Override
    public PageResult<Consumer> findByQuery(QPageQuery<QConsumer> query) throws Exception {
        QPageQuery<ConsumerQuery> pageQuery = new QPageQuery<>();
        pageQuery.setPagination(query.getPagination());
        pageQuery.setQuery(qConsumerConvert(query.getQuery()));
        String result = post(FINDBYQUERY_CONSUMER,pageQuery);
        PageResult<com.jd.journalq.domain.Consumer> pageResult = JSON.parseObject(result,new TypeReference<PageResult<com.jd.journalq.domain.Consumer>>(){});

        PageResult<Consumer> consumerPageResult = new PageResult<>();
        consumerPageResult.setPagination(pageResult.getPagination());
        consumerPageResult.setResult(pageResult.getResult().stream().map(consumer -> nsrConsumerConverter.revert(consumer)).collect(Collectors.toList()));
        return consumerPageResult;
    }
    @Override
    public List<Consumer> findByQuery(QConsumer qConsumer) throws Exception {
        String result = post(LIST_CONSUMER,qConsumerConvert(qConsumer));
        List<com.jd.journalq.domain.Consumer> consumerList = JSON.parseArray(result).toJavaList(com.jd.journalq.domain.Consumer.class);
        return consumerList.stream().map(consumer -> nsrConsumerConverter.revert(consumer)).collect(Collectors.toList());
    }
    @Override
    public Consumer findById(String  id) throws Exception {
        String result = post(GETBYID_CONSUMER,id);
        com.jd.journalq.domain.Consumer nsrConsumer = JSONObject.parseObject(result, com.jd.journalq.domain.Consumer.class);
        return nsrConsumerConverter.revert(nsrConsumer);
    }

    @Override
    public List<String> findAllSubscribeGroups() throws Exception {
        //todo 不要查全部consumer
        List<Consumer> consumerList = findByQuery(new QConsumer());
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
