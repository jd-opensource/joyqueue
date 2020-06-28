package org.joyqueue;

import com.google.common.collect.Lists;
import org.joyqueue.broker.BrokerService;
import org.joyqueue.broker.config.Args;
import org.joyqueue.broker.config.ConfigDef;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.producer.Produce;

import org.joyqueue.domain.*;
import org.joyqueue.helper.PortHelper;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.session.Producer;
import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.NameService;
import org.joyqueue.plugin.SingletonController;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestClusterBase extends Service {

    private String DEFAULT_JOYQUEUE="joyqueue";
    private String ROOT_DIR =System.getProperty("java.io.tmpdir")+ File.separator+DEFAULT_JOYQUEUE;
    private int brokerPort=50088;
    int portInterval=10000;
    private List<BrokerService> brokers=new ArrayList<>();
    @Before
    public void setup() throws Exception{
        SingletonController.forceCloseSingletion();
        SingletonController.closeClassSingletion(Consume.class);
        SingletonController.closeClassSingletion(NameService.class);
        SingletonController.closeClassSingletion(StoreService.class);
        SingletonController.closeClassSingletion(InternalServiceProvider.class);
    }
    /**
     * Launch a N node cluster
     * @param N node num
     * @param port  broker port
     *
     **/


    public void launch(int N, int port, int timeout, TimeUnit unit) throws Exception{
        String journalKeeperNodes = IpUtil.getLocalIp()+":"+String.valueOf(PortHelper.getJournalkeeperPort(port));
        for(int i=0;i<N;i++) {
            String rootDir=ROOT_DIR+File.separator+String.format("_%d",i);
            BrokerService broker=new BrokerService(args(port+i*portInterval,rootDir,journalKeeperNodes));
            broker.start();
            brokers.add(broker);
        }
        // wait cluster ready
        BrokerService b= brokers.get(0);
        long start= SystemClock.now();
        do {
           int cluster= b.getBrokerContext().getNameService().getAllBrokers().size();
           if(cluster==N){
               break;
           }else{
               if(SystemClock.now()-start<unit.toMillis(timeout)) {
                   Thread.sleep(1000);
               }else{
                   throw new IllegalStateException("Start cluster timeout");
               }
           }
        }while(true);
    }

    /**
     * Launch multi broker
     **/
    public void launch(int N) throws Exception{
        launch(N,brokerPort,5000,TimeUnit.MILLISECONDS);
    }


    /**
     * Build args
     **/
    public String[] args(int port,String applicationRoot,String journalKeeperNodes){
        Args args=new Args();
        args.append(ConfigDef.APPLICATION_DATA_PATH.key(),applicationRoot);
        args.append(ConfigDef.TRANSPORT_SERVER_PORT.key(),String.valueOf(port));
        args.append(ConfigDef.NAME_SERVER_JOURNAL_KEEPER_NODES.key(),journalKeeperNodes);
        return args.build();
    }


    /**
     * All broker
     **/
    public List<BrokerService> brokers(){
        return null;
    }


    @Test
    public void testCreateTopic() throws Exception{
        launch(3);
        createTopic("abcxx",(short) 24);
    }

    /**
     * Create topic on random select broker
     **/
    public void createTopic(String topic, short partitions) throws Exception{
       NameService ns= nameService();
       Set<Short> partitionSet=new HashSet();
       for(short i=0;i<partitions;i++){
           partitionSet.add(i);
       }
       List<Broker> brokers=ns.getAllBrokers();
       if(brokers.size()<=0) throw new IllegalStateException("No broker!");
       brokers=brokers.size()<3?brokers: shuffle(brokers,3);
       Topic t=new Topic();
             t.setName(new TopicName(topic));
             t.setPartitions(partitions);
       PartitionGroup partitionGroup=new PartitionGroup();
       partitionGroup.setTopic(new TopicName(topic));
       partitionGroup.setGroup(0);
       partitionGroup.setPartitions(partitionSet);
       partitionGroup.setReplicas(brokers.stream().map(Broker::getId).collect(Collectors.toSet()));
       ns.addTopic(t, Lists.newArrayList(partitionGroup));
    }

    /**
     *
     * Consume subscribe
     *
     **/
    public void produceSubscribe(String topic,String app) throws Exception{
        NameService ns= nameService();
        Subscription subscription=new Subscription();
        subscription.setTopic(new TopicName(topic));
        subscription.setApp(app);
        subscription.setType(Subscription.Type.PRODUCTION);
        ns.subscribe(subscription,ClientType.JOYQUEUE);
    }


    /**
     * Test send message
     **/
    @Test
    public void testSendMessage() throws Exception{
        String topic="abc";
        String app="aaaaa";
        launch(3);
        createTopic(topic,(short)24);
        produceSubscribe(topic,app);

        sendMessage(topic,app,"hello,test!",null);
    }

    /***
     * Wait metadata ready
     **/
    public void waitMetadataReady(BrokerService brokerService,String topic) throws Exception{
        int i=3;
        do {
            Thread.sleep(1000);
            List<Short> partitions = brokerService.getBrokerContext().getClusterManager().getMasterPartitionList(new TopicName(topic));
            if(partitions!=null){
                return;
            }
        }while(i-->0);
        throw new IllegalStateException(String.format("%s not ready",topic));
    }
    public void sendMessage(String topic,String app,String msg,String businessId) throws Exception{
          BrokerService leader=leader(topic,0);
          waitMetadataReady(leader,topic);

          Produce produce=leader.getBrokerContext().getProduce();
          Producer producer=new Producer();
          producer.setTopic(topic);
          producer.setApp(app);
          //producer.setClientType(Cl);
          BrokerMessage bm= create(topic,app,msg,businessId);
          produce.putMessage(producer,Lists.newArrayList(bm),QosLevel.REPLICATION);
    }

    /**
     * Broker message
     **/
    public BrokerMessage create(String topic, String app, String msg,String businessId){
        BrokerMessage bm=new BrokerMessage();
        bm.setTopic(topic);
        bm.setApp(app);
        bm.setBody(msg.getBytes());
        bm.setBusinessId(businessId);
        bm.setClientIp(IpUtil.getLocalIp().getBytes());
        return bm;
    }

    /**
     * Random select n broker
     *
     **/
    public List<Broker> shuffle(List<Broker> brokers,int n){
        Collections.shuffle(brokers);
        return brokers.subList(0,n);
    }


    /**
     * Get Name service
     **/
    public NameService nameService() throws Exception{
        if(brokers.size()<=0) throw new IllegalStateException("no broker exist");
        BrokerService b= brokers.get(0);
        return b.getBrokerContext().getNameService();
    }


    public BrokerService leader(String topic,int partitionGorup) throws Exception{
        NameService ns=nameService();
        TopicConfig tc=ns.getTopicConfig(new TopicName(topic));
        PartitionGroup pg=tc.getPartitionGroups().get(partitionGorup);
        if(pg.getLeader()==null) throw new IllegalStateException("Leader not found");
        for(BrokerService broker:brokers){
            if(broker.getBrokerContext().getBroker().getId().equals(pg.getLeader())){
                return broker;
            }
        }
        return null;
    }


    @Test
    public void launchTest() throws Exception{
        launch(3,40088,5,TimeUnit.SECONDS);
        BrokerService broker=brokers.get(0);
        Assert.assertNotNull(broker);
        Thread.sleep(3600*1000);
    }

    @After
    public void close() throws Exception{
        for(BrokerService b:brokers){
            b.stop();
        }
        Files.deleteDirectory(new File(ROOT_DIR));
    }
}
