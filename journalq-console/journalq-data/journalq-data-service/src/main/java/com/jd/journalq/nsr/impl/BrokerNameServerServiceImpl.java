package com.jd.journalq.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.convert.NsrBrokerConverter;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.query.QBroker;
import com.jd.journalq.nsr.model.BrokerQuery;
import com.jd.journalq.nsr.BrokerNameServerService;
import com.jd.journalq.nsr.NameServerBase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("brokerNameServerService")
public class BrokerNameServerServiceImpl extends NameServerBase implements BrokerNameServerService {

    public static final String ADD_BROKER="/broker/add";
    public static final String REMOVE_BROKER="/broker/remove";
    public static final String UPDATE_BROKER="/broker/update";
    public static final String LIST_BROKER="/broker/list";
    public static final String GETBYID_BROKER="/broker/getById";
    public static final String GETBYIDS_BROKER="/broker/getByIds";
    public static final String FINDBYQUERY_BROKER="/broker/findByQuery";
    NsrBrokerConverter nsrBrokerConverter = new NsrBrokerConverter();

    @Override
    public Broker findById(Long id) throws Exception {
        String result = post(GETBYID_BROKER,id);
        com.jd.journalq.domain.Broker nsrBroker = JSON.parseObject(result, com.jd.journalq.domain.Broker.class);
        return nsrBrokerConverter.revert(nsrBroker);
    }

    @Override
    public PageResult<Broker> findByQuery(QPageQuery<QBroker> query) {
        QPageQuery<BrokerQuery> queryQPageQuery = new QPageQuery<>();
        queryQPageQuery.setPagination(query.getPagination());
        queryQPageQuery.setQuery(brokerQueryConvert(query.getQuery()));
        try {
            PageResult<Broker> pageResult = new PageResult<>();
            String result = post(FINDBYQUERY_BROKER,queryQPageQuery);
            PageResult<com.jd.journalq.domain.Broker> brokerPageResult = JSON.parseObject(result,new TypeReference<PageResult<com.jd.journalq.domain.Broker>>(){});
            pageResult.setPagination(brokerPageResult.getPagination());
            pageResult.setResult(brokerPageResult.getResult().stream().map(broker -> nsrBrokerConverter.revert(broker)).collect(Collectors.toList()));
            return pageResult;
        } catch (Exception e) {
            logger.error("findByQuery exception",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加broker
     * @param broker
     * @throws Exception
     */
    @Override
    public int add(Broker broker) throws Exception {
        com.jd.journalq.domain.Broker nsrBroker = nsrBrokerConverter.convert(broker);
        String result =  postWithLog(ADD_BROKER, nsrBroker,OperLog.Type.BROKER.value(),OperLog.OperType.ADD.value(),String.valueOf(broker.getId()));
        return isSuccess(result);
    }
    /**
     * 更新broker
     * @param broker
     * @throws Exception
     */
    @Override
    public int update(Broker broker) throws Exception {
        String result = post(GETBYID_BROKER,broker.getId());
        com.jd.journalq.domain.Broker nsrBroker = JSON.parseObject(result, com.jd.journalq.domain.Broker.class);

        if(nsrBroker == null) {
            nsrBroker = new com.jd.journalq.domain.Broker();
        }
        if (broker.getIp() != null) {
            nsrBroker.setIp(broker.getIp());
        }
        if (broker.getRetryType() != null) {
            nsrBroker.setRetryType(broker.getRetryType());
        }
        if (broker.getPort() > 0) {
            nsrBroker.setPort(broker.getPort());
        }
        nsrBroker.setId(Long.valueOf(broker.getId()).intValue());
        //nsrBroker.setDataCenter(broker.getDataCenter().getCode());
        String result1 = postWithLog(UPDATE_BROKER, nsrBroker,OperLog.Type.BROKER.value(),OperLog.OperType.UPDATE.value(),String.valueOf(broker.getId()));
        return isSuccess(result1);
    }

    @Override
    public List<Broker> findByQuery(QBroker query) throws Exception {
        BrokerQuery brokerQuery = brokerQueryConvert(query);
        List<com.jd.journalq.domain.Broker> nsrBrokers = JSONArray.parseArray(post(LIST_BROKER,brokerQuery), com.jd.journalq.domain.Broker.class);
        return nsrBrokers.stream().map(broker -> nsrBrokerConverter.revert(broker)).collect(Collectors.toList());
    }

    @Override
    public List<Broker> getByIdsBroker(List<Integer> ids) throws Exception {
        String result = post(GETBYIDS_BROKER,ids);

        List<com.jd.journalq.domain.Broker> brokerList = JSON.parseArray(result, com.jd.journalq.domain.Broker.class);
        if (brokerList == null || brokerList.size() <=0) return null;
        return brokerList.stream().map(broker -> nsrBrokerConverter.revert(broker)).collect(Collectors.toList());
    }

    @Override
    public List<Broker> syncBrokers() throws Exception {
        List<com.jd.journalq.domain.Broker>  nsrBrokers = JSONArray.parseArray(post(LIST_BROKER,null), com.jd.journalq.domain.Broker.class);
        List<Broker> brokerList = new ArrayList<>(nsrBrokers.size());
        nsrBrokers.forEach(nsrBroker->{
            Broker broker = new Broker();
            broker.setId(nsrBroker.getId());
            broker.setIp(nsrBroker.getIp());
            broker.setPort(nsrBroker.getPort());
            broker.setRetryType(nsrBroker.getRetryType());
            broker.setDataCenter(new Identity(0L,"UNKNOW"));
            brokerList.add(broker);
        });
        return brokerList;
    }
    /**
     * 删除broker
     * @param broker
     * @throws Exception
     */
    @Override
    public int delete(Broker broker) throws Exception {
        com.jd.journalq.domain.Broker nsrBroker = new com.jd.journalq.domain.Broker();
        nsrBroker.setIp(broker.getIp());
        nsrBroker.setRetryType(broker.getRetryType());
        nsrBroker.setPort(broker.getPort());
        nsrBroker.setId(Long.valueOf(broker.getId()).intValue());
        String result = postWithLog(REMOVE_BROKER, nsrBroker,OperLog.Type.BROKER.value(),OperLog.OperType.DELETE.value(),String.valueOf(broker.getId()));
        return isSuccess(result);
    }
    private BrokerQuery brokerQueryConvert(QBroker query){
        BrokerQuery brokerQuery = new BrokerQuery();
        if (query != null) {
            brokerQuery.setBrokerId(query.getBrokerId());
            brokerQuery.setIp(query.getIp());
            brokerQuery.setKeyword(query.getKeyword());
            brokerQuery.setBrokerList(query.getInBrokerIds());
        }
        return brokerQuery;
    }
}
