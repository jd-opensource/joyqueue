package com.jd.journalq.nsr.admin;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.nsr.NsrAdmin;
import com.jd.journalq.nsr.utils.AsyncHttpClient;
import java.io.IOException;
import java.util.List;

public class AdminClient implements NsrAdmin {
    private AppAdmin appAdmin=new AppAdmin();
    private TopicAdmin topicAdmin=new TopicAdmin();
    private BrokerAdmin brokerAdmin=new BrokerAdmin();
    private String host;
    public AdminClient(String host){
        this.host=host;
    }
    @Override
    public String publish(TopicAdmin.PubSubArg pubSubArg) throws Exception{
        pubSubArg.host=host;
        return topicAdmin.publish(pubSubArg,null);
    }

    @Override
    public String subscribe(TopicAdmin.PubSubArg pubSubArg) throws Exception{
        pubSubArg.host=host;
        return topicAdmin.subscribe(pubSubArg,null);
    }

    @Override
    public String unPublish(TopicAdmin.PubSubArg pubSubArg) throws Exception {
        pubSubArg.host=host;
        return topicAdmin.unPublish(pubSubArg,null);
    }

    @Override
    public String unSubscribe(TopicAdmin.PubSubArg pubSubArg) throws Exception {
        pubSubArg.host=host;
        return topicAdmin.unSubscribe(pubSubArg,null);
    }

    @Override
    public String delTopic(TopicAdmin.TopicArg topicArg) throws Exception {
        topicArg.host=host;
        return topicAdmin.delete(topicArg,null);
    }

    @Override
    public String createTopic(TopicAdmin.TopicArg topicArg) throws Exception{
        topicArg.host=host;
        return topicAdmin.add(topicArg,null);
    }

    @Override
    public String token(AppAdmin.TokenArg tokenArg) throws Exception{
        tokenArg.host=host;
        return appAdmin.token(tokenArg,null);
    }

    @Override
    public List<Broker> listBroker(BrokerAdmin.ListArg listArg) throws Exception {
        listArg.host=host;
        return brokerAdmin.list(listArg,null);
    }

    @Override
    public void close() throws IOException {
        AsyncHttpClient.close();
    }
}
