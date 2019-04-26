package com.jd.journalq.nsr.admin;


import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.Subscription;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.stream.Collectors;

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminClientTest {

    private static final String host="http://127.0.0.1:50091";
    private static final String topic="test_topic_0";
    private static final String app="test_app_0";
    static AdminClient client;
    @BeforeClass
    public static void setUp() throws Exception{
        client=new AdminClient(host);
        BrokerAdmin.ListArg listArg=new BrokerAdmin.ListArg();
        TopicAdmin.TopicArg topicArg=new TopicAdmin.TopicArg();
        topicArg.code=topic;
        List<Broker> brokers=client.listBroker(listArg);
        Assert.assertNotEquals(null,brokers);
        topicArg.brokers=brokers.stream().map(broker -> broker.getId()).collect(Collectors.toList());
        client.createTopic(topicArg);
    }

    @Test
    public void publish() throws Exception{
        TopicAdmin.PubSubArg pubSubArg=new TopicAdmin.PubSubArg();
        pubSubArg.subscribe.topic=topic;
        pubSubArg.subscribe.app=app;
        pubSubArg.subscribe.type=(int)Subscription.Type.PRODUCTION.getValue();
        String result=client.publish(pubSubArg);
        Assert.assertEquals("success",result);
        result=client.unPublish(pubSubArg);
        Assert.assertEquals("success",result);
    }



    @Test
    public void subscribe() throws Exception{
        TopicAdmin.PubSubArg pubSubArg=new TopicAdmin.PubSubArg();
        pubSubArg.subscribe.topic=topic;
        pubSubArg.subscribe.app=app;
        pubSubArg.subscribe.type=(int)Subscription.Type.CONSUMPTION.getValue();
        String result=client.subscribe(pubSubArg);
        Assert.assertEquals("success",result);
        result=client.unSubscribe(pubSubArg);
        Assert.assertEquals("success",result);
    }


    @AfterClass
    public static void clean() throws Exception{
        // remove topic
        TopicAdmin.TopicArg topicArg=new TopicAdmin.TopicArg();
        topicArg.code=topic;
        String result=client.delTopic(topicArg);
        Assert.assertEquals("success",result);
        client.close();
    }



}
