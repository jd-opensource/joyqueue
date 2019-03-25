package com.jd.journalq.service.impl;

import com.jd.journalq.model.ListQuery;
import com.jd.journalq.exception.ServiceException;
import com.jd.journalq.exception.ValidationException;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.Consumer;
import com.jd.journalq.model.domain.Producer;
import com.jd.journalq.model.domain.TopicUnsubscribedApplication;
import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.model.query.QProducer;
import com.jd.journalq.nsr.ConsumerNameServerService;
import com.jd.journalq.nsr.ProducerNameServerService;
import com.jd.journalq.repository.ApplicationRepository;
import com.jd.journalq.service.ApplicationService;
import com.jd.journalq.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.jd.journalq.exception.ServiceException.BAD_REQUEST;
import static com.jd.journalq.exception.ValidationException.UNIQUE_EXCEPTION_STATUS;

/**
 * Created by yangyang115 on 18-7-27.
 */
@Service("applicationService")
public class ApplicationServiceImpl extends PageServiceSupport<Application, QApplication,ApplicationRepository> implements ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private ProducerNameServerService producerNameServerService;
    @Autowired
    private ConsumerNameServerService consumerNameServerService;

    @Override
    public int add(Application app) {
        //Validate unique
        Application apps = findByCode(app.getCode());
        if (NullUtil.isNotEmpty(apps)) {
            throw new ValidationException(UNIQUE_EXCEPTION_STATUS, getUniqueExceptionMessage());
        }
        //Add
        return super.add(app);
    }

    @Override
    public Application findByCode(final String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        return repository.findByCode(code);
    }

    @Override
    public PageResult<Application> findSubscribedByQuery(QPageQuery<QApplication> query) {
        if (query == null || query.getQuery() == null) {
            return PageResult.empty();
        }
        ListQuery<QApplication> listQuery = new ListQuery<>();
        listQuery.setQuery(query.getQuery());
        List<Application> applicationList = findByQuery(listQuery);
        List<String> appList = getSubscribeList(query.getQuery());
        if (applicationList != null && appList != null) {
            applicationList = applicationList.stream().filter(application -> appList.contains(application.getCode())).collect(Collectors.toList());
        }
        return new PageResult<>(query.getPagination(),applicationList);
    }
    //
//    @Override
//    public List<Application> findWithDeletedByCode(final String code) {
//        if (code == null || code.isEmpty()) {
//            return null;
//        }
//        return repository.findWithDeletedByCode(code);
//    }

    @Override
    public PageResult<Application> findUnsubscribedByQuery(QPageQuery<QApplication> query) {
        if (query == null || query.getQuery() == null) {
            return PageResult.empty();
        }
        query.getQuery().setNoInCodes(getSubscribeList(query.getQuery()));
        return repository.findUnsubscribedByQuery(query);
    }

    @Override
    public PageResult<TopicUnsubscribedApplication> findTopicUnsubscribedByQuery(QPageQuery<QApplication> query) {
        if (query == null || query.getQuery() == null) {
            return PageResult.empty();
        }
        if (query.getQuery() == null || query.getQuery().getSubscribeType() == null || query.getQuery().getTopic() == null
                || query.getQuery().getTopic().getCode() == null) {
            throw new ServiceException(BAD_REQUEST, "bad QApplication query argument.");
        }
        query.getQuery().setNoInCodes(getSubscribeList(query.getQuery()));
        PageResult<Application> applicationPageResult = repository.findUnsubscribedByQuery(query);
        if (NullUtil.isEmpty(applicationPageResult.getResult())) {
            return PageResult.empty();
        }
        return new PageResult(applicationPageResult.getPagination(), applicationPageResult.getResult().stream().map(app -> {
            TopicUnsubscribedApplication topicUnsubscribedApp = new TopicUnsubscribedApplication(app);
            topicUnsubscribedApp.setTopicCode(query.getQuery().getTopic().getCode());
            topicUnsubscribedApp.setSubscribeType(query.getQuery().getSubscribeType());
            if (query.getQuery().getSubscribeType() == Consumer.CONSUMER_TYPE) {
                //find consumer list by topic and app refer, then set showDefaultSubscribeGroup property
                QConsumer qConsumer = new QConsumer();
                qConsumer.setTopic(query.getQuery().getTopic());
                qConsumer.setNamespace(query.getQuery().getTopic().getNamespace().getCode());
                qConsumer.setReferer(app.getCode());
                try {
                    List<Consumer> consumers = consumerNameServerService.findByQuery(qConsumer);
                    topicUnsubscribedApp.setSubscribeGroupExist((consumers == null || consumers.size() < 1) ? false : true);
                } catch (Exception e) {
                    logger.error("can not find consumer list by topic and app refer.", e);
                    topicUnsubscribedApp.setSubscribeGroupExist(true);
                }
            }
            return topicUnsubscribedApp;
        }).collect(Collectors.toList()));
    }

    private List<String> getSubscribeList(QApplication query){
        try{
            List<String> noInCodes = null;
            if (query.getSubscribeType() != null) {
                if (query.getSubscribeType() == Producer.PRODUCER_TYPE) {
                    QProducer qProducer = new QProducer();

                    if (query.getTopic() != null) {
                        qProducer.setTopic(query.getTopic());
                    }
                    List<Producer> producerList = producerNameServerService.findByQuery(qProducer);
                    if (producerList == null)return null;
                    noInCodes = producerList.stream().map(producer -> producer.getApp().getCode()).collect(Collectors.toList());
                }
//                if (query.getSubscribeType() == Consumer.CONSUMER_TYPE) {
////                    QConsumer qConsumer = new QConsumer();
////                    qConsumer.setTopic(query.getTopic());
////                    List<Consumer> consumerList = consumerNameServerService.findByQuery(qConsumer);
////                    if (consumerList == null)return null;
////                    noInCodes = consumerList.stream().map(producer -> producer.getApp().getCode()).collect(Collectors.toList());
////                }
            }
            return noInCodes;
        } catch (Exception e) {
            logger.error("getNoInCodes",e);
        }
        return null;
    }

    @Override
    public List<Application> findByCodes(List<String> codes) {
        return  repository.findByCodes(codes);
    }
}
