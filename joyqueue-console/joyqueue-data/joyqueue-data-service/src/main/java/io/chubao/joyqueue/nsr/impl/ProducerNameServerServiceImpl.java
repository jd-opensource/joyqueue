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
package io.chubao.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.chubao.joyqueue.convert.CodeConverter;
import io.chubao.joyqueue.convert.NsrProducerConverter;
import io.chubao.joyqueue.domain.ClientType;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.query.QProducer;
import io.chubao.joyqueue.nsr.NameServerBase;
import io.chubao.joyqueue.nsr.ProducerNameServerService;
import io.chubao.joyqueue.nsr.model.ProducerQuery;
import io.chubao.joyqueue.toolkit.security.EscapeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.chubao.joyqueue.model.domain.Namespace.DEFAULT_NAMESPACE_CODE;
import static io.chubao.joyqueue.model.domain.Namespace.DEFAULT_NAMESPACE_ID;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("producerNameServerService")
public class ProducerNameServerServiceImpl extends NameServerBase implements ProducerNameServerService {

    public static final String ADD_PRODUCER="/producer/add";
    public static final String UPDATE_PRODUCER="/producer/update";
    public static final String REMOVE_PRODUCER="/producer/remove";
    public static final String PRODUCER_GETALL_BY_CLIENTTYPE="/producer/list";
    public static final String LIST_PRODUCER="/producer/getList";
    public static final String GETBYID_PRODUCER="/producer/getById";
    public static final String FINDBYQUERY_PRODUCER="/producer/findByQuery";

    private NsrProducerConverter nsrProducerConverter = new NsrProducerConverter();

    /**
     * 添加producer
     * @param producer
     * @throws Exception
     */
    @Override
    public int add(Producer producer) throws Exception {
        io.chubao.joyqueue.domain.Producer nsrProducer = nsrProducerConverter.convert(producer);
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
        io.chubao.joyqueue.domain.Producer nsrProducer = nsrProducerConverter.convert(producer);
        String result1 = postWithLog(UPDATE_PRODUCER, nsrProducer,OperLog.Type.PRODUCER.value(),OperLog.OperType.UPDATE.value(),producer.getTopic().getCode());
        return isSuccess(result1);
    }

    @Override
    public List<Producer> findByQuery(QProducer query) throws Exception {
        ProducerQuery producerQuery = producerQueryConvert(query);
        return getListProducer(producerQuery);
    }

    /**
     * 删除producer
     * @param producer
     * @throws Exception
     */
    @Override
    public int delete(Producer producer) throws Exception {
        io.chubao.joyqueue.domain.Producer nsrProducer = new io.chubao.joyqueue.domain.Producer();
        nsrProducer.setApp(producer.getApp().getCode());
        nsrProducer.setClientType(ClientType.valueOf(producer.getClientType()));
        nsrProducer.setTopic(CodeConverter.convertTopic(producer.getNamespace(),producer.getTopic()));
        String result = postWithLog(REMOVE_PRODUCER, nsrProducer,OperLog.Type.PRODUCER.value(),OperLog.OperType.DELETE.value(),producer.getTopic().getCode());
        return isSuccess(result);
    }

    @Override
    public List<Producer> syncProducer(byte clientType) throws Exception {
        JSONObject request = new JSONObject();
        request.put("client_type",clientType);
        List<io.chubao.joyqueue.domain.Producer>  nsrProducers = JSONArray.parseArray(post(PRODUCER_GETALL_BY_CLIENTTYPE,request), io.chubao.joyqueue.domain.Producer.class);
        List<Producer> producerList = new ArrayList<>(nsrProducers.size());
        nsrProducers.forEach(nsrProducer->{
            Producer producer = new Producer();
            TopicName nt = nsrProducer.getTopic();
            producer.setApp(new Identity(null,nsrProducer.getApp()));
            if(nt.getNamespace().equals(TopicName.DEFAULT_NAMESPACE)){
                producer.setNamespace(new Namespace(DEFAULT_NAMESPACE_ID, DEFAULT_NAMESPACE_CODE));
            }else{
                producer.setNamespace(new Namespace(nt.getNamespace()));
            }
            producer.setTopic(new Topic(null,EscapeUtils.reEscapeTopic(nt.getCode())));
            //producer.setSubscribeGroup(ag[1]);
            producer.setClientType(nsrProducer.getClientType().value());
            producerList.add(producer);
        });
        return producerList;
    }

    @Override
    public PageResult<Producer> findByQuery(QPageQuery<QProducer> query) throws Exception {
        QPageQuery<ProducerQuery> pageQuery = new QPageQuery<>();
        pageQuery.setPagination(query.getPagination());
        pageQuery.setQuery(producerQueryConvert(query.getQuery()));
        String result = post(FINDBYQUERY_PRODUCER,pageQuery);
        PageResult<io.chubao.joyqueue.domain.Producer> pageResult = JSON.parseObject(result,new TypeReference<PageResult<io.chubao.joyqueue.domain.Producer>>(){});

        PageResult<Producer> producerPageResult = new PageResult<>();
        producerPageResult.setPagination(pageResult.getPagination());
        producerPageResult.setResult(pageResult.getResult().stream().map(producer -> nsrProducerConverter.revert(producer)).collect(Collectors.toList()));
        return producerPageResult;
    }
    @Override
    public List<Producer> getListProducer(ProducerQuery producerQuery) throws Exception {
        String result = post(LIST_PRODUCER,producerQuery);
        List<io.chubao.joyqueue.domain.Producer> producerList = JSON.parseArray(result).toJavaList(io.chubao.joyqueue.domain.Producer.class);
        return producerList.stream().map(producer -> nsrProducerConverter.revert(producer)).collect(Collectors.toList());
    }
    @Override
    public Producer findById(String nsrProducerId) throws Exception {
        String result = post(GETBYID_PRODUCER,nsrProducerId);
        io.chubao.joyqueue.domain.Producer nsrProducer = JSONObject.parseObject(result, io.chubao.joyqueue.domain.Producer.class);
        return nsrProducerConverter.revert(nsrProducer);
    }

    @Override
    public Producer findByTopicAppGroup(String namespace, String topic, String app) {
        ProducerQuery producerQuery = new ProducerQuery();
        producerQuery.setApp(app);
        producerQuery.setNamespace(namespace);
        producerQuery.setTopic(topic);
        try {
            List<Producer> list = getListProducer(producerQuery);
            if (list == null || list.size() <=0) return null;
            return list.get(0);
        } catch (Exception e) {
            throw new ServiceException(ServiceException.IGNITE_RPC_ERROR,e.getMessage());
        }
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
