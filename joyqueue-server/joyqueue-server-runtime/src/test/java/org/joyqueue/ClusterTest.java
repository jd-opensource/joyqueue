package org.joyqueue;

import org.joyqueue.broker.BrokerService;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.producer.Produce;
import org.junit.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClusterTest extends ClusterTestBase {
    @Before
    public  void setup() throws Exception{
        start();
        launchCluster(3);
//        launchJournalKeeperCluster(3);
    }
    /**
     * All broker
     **/
    public List<BrokerService> brokers(){
        return null;
    }

    @Ignore
    @Test
    public void testCreateTopic() throws Exception{
        createTopic("testCreateTopic",(short) 24);
    }

    /**
     * Test send message
     **/
    @Ignore
    @Test
    public void testSendMessage() throws Exception{
        String topic="testSendMessage";
        String app="aaaaa";
        createTopic(topic,(short)24);
        produceSubscribe(topic,app);
        BrokerService leader= waitLeaderReady(topic,0,30,TimeUnit.SECONDS);
        waitMetadataReady(leader,topic,30,TimeUnit.SECONDS);
        Produce produce=leader.getBrokerContext().getProduce();
        int messagesCount=100;
        for(int i=0;i<messagesCount;i++) {
            simpleProduceMessage(produce,topic, app, "hello,test!", null);
        }
    }

    /**
     * For launch local cluster and hold
     **/
    @Ignore
    @Test
    public void testLaunchCluster() throws Exception{
          Thread.sleep(3600*1000);
    }

    @Test
    public void testProduceAndConsume() throws Exception{
        String topic="testProduceAndConsume";
        String app="aaaaa";
        System.out.println("creating topic,"+topic);
        createTopic(topic,(short)24);
        produceSubscribe(topic,app);
        BrokerService leader= waitLeaderReady(topic,0,60,TimeUnit.SECONDS);
        waitMetadataReady(leader,topic,30,TimeUnit.SECONDS);
        Produce produce=leader.getBrokerContext().getProduce();
        Consume consume=leader.getBrokerContext().getConsume();
        // consume subscribe
        consumeSubscribe(topic,app);
        waitConsumeSubscribeReady(consume,topic,app,0,24,60,TimeUnit.SECONDS);
        int messagesCount=1000;
        for(int i=0;i<messagesCount;i++) {
            simpleProduceMessage(produce,topic, app, "hello,test!", null);
        }
        leader= waitLeaderReady(topic,0,60,TimeUnit.SECONDS);
        waitMetadataReady(leader,topic,30,TimeUnit.SECONDS);
        // consume
        simpleConsumeMessage(consume,topic,app);
    }



    @After
    public void close() throws Exception{
        super.stop();
    }
}
