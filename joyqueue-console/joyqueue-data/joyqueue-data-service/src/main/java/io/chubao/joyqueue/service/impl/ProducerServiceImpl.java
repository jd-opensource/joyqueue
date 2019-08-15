package io.chubao.joyqueue.service.impl;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.query.QApplication;
import io.chubao.joyqueue.model.query.QProducer;
import io.chubao.joyqueue.service.ApplicationService;
import io.chubao.joyqueue.service.ProducerService;
import io.chubao.joyqueue.nsr.ProducerNameServerService;
import io.chubao.joyqueue.nsr.TopicNameServerService;
import com.google.common.base.Preconditions;
import io.chubao.joyqueue.util.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public PageResult<Producer> findByQuery(QPageQuery<QProducer> query) throws Exception {
        User user = LocalSession.getSession().getUser();
        if (user.getRole() == User.UserRole.NORMAL.value()) {
            QApplication qApplication = new QApplication();
            qApplication.setUserId(user.getId());
            qApplication.setAdmin(false);
            List<Application> applicationList = applicationService.findByQuery(new ListQuery<>(qApplication));
            if (applicationList == null || applicationList.size() <=0 ) return PageResult.empty();
            List<String> appCodes = applicationList.stream().map(application -> application.getCode()).collect(Collectors.toList());
            query.getQuery().setAppList(appCodes);
        }
        return producerNameServerService.findByQuery(query);
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
    public List<Producer> findByQuery(QProducer query) throws Exception {
        return producerNameServerService.findByQuery(query);
    }

    @Override
    public Producer findByTopicAppGroup(String namespace, String topic, String app) {
        return producerNameServerService.findByTopicAppGroup(namespace,topic,app);
    }

    private void checkArgument(Producer producer) {
        Preconditions.checkArgument(producer != null, "invalidate producer arg.");
    }

}

