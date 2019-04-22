package com.jd.journalq.nsr;


import com.jd.journalq.domain.Broker;
import com.jd.journalq.nsr.admin.AppAdmin;
import com.jd.journalq.nsr.admin.BrokerAdmin;
import com.jd.journalq.nsr.admin.TopicAdmin;

import java.io.Closeable;
import java.util.List;

public interface NsrAdmin extends Closeable {
    String publish(TopicAdmin.PubSubArg pubSubArg) throws Exception;
    String unPublish(TopicAdmin.PubSubArg pubSubArg) throws Exception;
    String subscribe(TopicAdmin.PubSubArg pubSubArg) throws Exception;
    String unSubscribe(TopicAdmin.PubSubArg pubSubArg) throws Exception;

    String createTopic(TopicAdmin.TopicArg topicArg) throws Exception;
    String delTopic(TopicAdmin.TopicArg topicArg) throws Exception;
    String token(AppAdmin.TokenArg tokenArg) throws Exception;
    List<Broker> listBroker(BrokerAdmin.ListArg listArg) throws Exception;

}
