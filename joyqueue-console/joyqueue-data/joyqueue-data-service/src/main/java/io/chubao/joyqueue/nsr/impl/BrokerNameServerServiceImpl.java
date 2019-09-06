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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import io.chubao.joyqueue.convert.NsrBrokerConverter;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QBroker;
import io.chubao.joyqueue.nsr.BrokerNameServerService;
import io.chubao.joyqueue.nsr.NameServerBase;
import io.chubao.joyqueue.nsr.model.BrokerQuery;
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
    public static final String SEARCH_BROKER="/broker/search";
    NsrBrokerConverter nsrBrokerConverter = new NsrBrokerConverter();

    @Override
    public Broker findById(Integer id) throws Exception {
        String result = post(GETBYID_BROKER,id);
        io.chubao.joyqueue.domain.Broker nsrBroker = JSON.parseObject(result, io.chubao.joyqueue.domain.Broker.class);
        return nsrBrokerConverter.revert(nsrBroker);
    }

    @Override
    public PageResult<Broker> search(QPageQuery<QBroker> query) {
        QPageQuery<BrokerQuery> queryQPageQuery = new QPageQuery<>();
        queryQPageQuery.setPagination(query.getPagination());
        queryQPageQuery.setQuery(brokerQueryConvert(query.getQuery()));
        try {
            PageResult<Broker> pageResult = new PageResult<>();
            String result = post(SEARCH_BROKER,queryQPageQuery);
            PageResult<io.chubao.joyqueue.domain.Broker> brokerPageResult = JSON.parseObject(result,new TypeReference<PageResult<io.chubao.joyqueue.domain.Broker>>(){});
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
        io.chubao.joyqueue.domain.Broker nsrBroker = nsrBrokerConverter.convert(broker);
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
        io.chubao.joyqueue.domain.Broker nsrBroker = JSON.parseObject(result, io.chubao.joyqueue.domain.Broker.class);

        if(nsrBroker == null) {
            nsrBroker = new io.chubao.joyqueue.domain.Broker();
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
        if (broker.getPermission() != null) {
            nsrBroker.setPermission(io.chubao.joyqueue.domain.Broker.PermissionEnum.value(broker.getPermission()));
        }
        nsrBroker.setId(Long.valueOf(broker.getId()).intValue());
        //nsrBroker.setDataCenter(broker.getDataCenter().getCode());
        String result1 = postWithLog(UPDATE_BROKER, nsrBroker,OperLog.Type.BROKER.value(),OperLog.OperType.UPDATE.value(),String.valueOf(broker.getId()));
        return isSuccess(result1);
    }

    @Override
    public List<Broker> getByIdsBroker(List<Integer> ids) throws Exception {
        String result = post(GETBYIDS_BROKER,ids);

        List<io.chubao.joyqueue.domain.Broker> brokerList = JSON.parseArray(result, io.chubao.joyqueue.domain.Broker.class);
        if (brokerList == null || brokerList.size() <=0) return null;
        return brokerList.stream().map(broker -> nsrBrokerConverter.revert(broker)).collect(Collectors.toList());
    }

    @Override
    public List<Broker> syncBrokers() throws Exception {
        List<io.chubao.joyqueue.domain.Broker>  nsrBrokers = JSONArray.parseArray(post(LIST_BROKER,null), io.chubao.joyqueue.domain.Broker.class);
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
        io.chubao.joyqueue.domain.Broker nsrBroker = new io.chubao.joyqueue.domain.Broker();
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
