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
package org.joyqueue.nsr.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.joyqueue.nsr.AdminConfig;
import org.joyqueue.nsr.CommandArgs;
import org.joyqueue.nsr.model.PartitionGroupQuery;
import org.joyqueue.nsr.utils.AsyncHttpClient;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TopicAdmin extends AbstractAdmin {
    private  static final Logger logger= LoggerFactory.getLogger(TopicAdmin.class);
    private AsyncHttpClient httpClient;
    public TopicAdmin(){
        this(new AsyncHttpClient());
    }

    public TopicAdmin(AsyncHttpClient httpClient){
        this.httpClient=httpClient;
    }

    @Parameters(separators = "=", commandDescription = "Topic arguments")
    public static class TopicArg extends CommandArgs {

        @Parameter(names = { "-c", "--code" }, description = "Topic code", required = true)
        public String code;

        @Parameter(names = { "-n", "--namespace" }, description = "Topic namespace", required = false)
        public String namespace="";

        @Parameter(names = { "-t", "--type" }, description = "Topic type:0 TOPIC,1 broadcast,2 sequential", required = false)
        public Integer type=0;

        @Parameter(names = { "-e", "--election" }, description = "Topic election type:0 raft,1 fix", required = false)
        public Integer election=0;
        @Parameter(names = { "-p", "--partitions" },description = "Topic partition numbers", required = false)
        public Integer partitions=5;

        @Parameter(names = { "-g", "--groups" }, description = "Topic partition group json format list", required = false)
        public List<String> partitionGroup;

        @Parameter(names = { "-b", "--brokers" }, description = "Topic optional brokers id", required = false)
        public List<Integer> brokers=new ArrayList<>();
    }


    public static class PartitionGroupArg extends CommandArgs{
        @Parameter(names = { "-c", "--topic" }, description = "Topic code", required = true)
        public String topic;
        @Parameter(names = { "-n", "--namespace" }, description = "Topic namespace", required = false)
        public String namespace="";
    }


    /**
     *  Subscription args
     *
     **/
    public static class SubscriptionArg {
        @Parameter(names = { "-c", "--topic" }, description = "Topic code", required = true)
        public String topic;

        @Parameter(names = { "-a", "--app" }, description = "Topic code", required = true)
        public String app;

        @Parameter(names = { "-n", "--namespace" }, description = "Topic namespace", required = false)
        public String namespace="";

        @Parameter(names = { "-t", "--type" }, description = "Topic type:1 produce,2 consume", required = false)
        public Integer type;

        @Parameter(names = {  "--client" }, description = "client type: 0 jmq,1 kafka,2 mqtt,other  ", required = false)
        public Integer client=0;
    }


    /**
     *  Subscription args
     *
     **/
    public static class ConsumerPolicyArg {

        // 就近发送
        @Parameter(names = { "--nearby" }, description = "near by consume", required = false)
        public boolean nearby;
        // 是否暂停消费

        @Parameter(names = { "--pause" }, description = "pause consume", required = false)
        public boolean paused;

        // 是否需要归档,默认归档
        @Parameter(names = { "--archive" }, description = "archive consume", required = false)
        public boolean archive;
        // 是否需要重试，默认重试

        @Parameter(names = { "--retry" }, description = " consumen retry,default true", required = false)
        public boolean retry=true;

        // 应答超时时间
        @Parameter(names = { "--timeout" }, description = " consume timeout", required = false)
        private Integer ackTimeout = 12000;
        // 批量大小
        @Parameter(names = { "--batch" }, description = " batch sieze size", required = false)
        public Integer batchSize=10;

        //并行消费预取数量
        @Parameter(names = { "--concurrent" }, description = " concurrent prefetch size", required = false)
        public Integer concurrent=1;


        //延迟消费
        @Parameter(names = { "--delay" }, description = " delay timeout ", required = false)
        public Integer delay = 0;
        //黑名单

        @Parameter(names = { "--blacklist" }, description = "black list ", required = false)
        public String blackList;
        //出错次数

        @Parameter(names = { "--weight" }, description = " weight ", required = false)
        public String weight;
    }


    /**
     *  Subscription args
     *
     **/
    public static class ProducerPolicyArg {

        @Parameter(names = { "--nearby" }, description = "consume from near", required = false)
        private boolean nearby;
        //单线程发送

        @Parameter(names = { "--single" }, description = "single thread produce", required = false)
        private boolean single;

        // 是否需要归档,默认归档
        @Parameter(names = { "--archive" }, description = "archive produce", required = false)
        private boolean archive;

        /**
         * 黑名单
         */
        @Parameter(names = { "--blackList" }, description = "black list", required = false)
        public String blackList;

        @Parameter(names = { "--timeout" }, description = "timeout ", required = false)
        public Integer timeout=1000;

        @Parameter(names = { "--weight" }, description = " weight ", required = false)
        public String weight;

    }






    @Parameters(separators = "=", commandDescription = "Publish args")
    public static class PublishArg extends CommandArgs{
        @ParametersDelegate
        public SubscriptionArg subscribe=new SubscriptionArg();

        @ParametersDelegate
        public ProducerPolicyArg  producerPolicy =new ProducerPolicyArg();

    }


    @Parameters(separators = "=", commandDescription = "Subscribe args")
    public static class SubscribeArg extends CommandArgs{
        @ParametersDelegate
        public SubscriptionArg subscribe=new SubscriptionArg();

        @ParametersDelegate
        public ConsumerPolicyArg  consumerPolicyArg =new ConsumerPolicyArg();

    }



    //192.168.73.147 1543486432  1543486547 "-b" ,"1543486432","-b","1543486547"
    // local 1556178070
    String[] argV={"add", "-c", "test_topic_bh_6" ,"--host","http://localhost:50091", "-b" ,"1556178070"};
    //String[] argV={"publish", "--topic", "test_topic_bh_6" ,"--app","test_app","--type","1","--host","http://localhost:50091"};

    public static void main(String[] args){
        String[] argV={"subscribe", "--topic", "test_topic_bh_6" ,
                "--app","test_app","--type","1","--host","http://localhost:50091","--delay","10000"};
//        String[] argV={"add", "-c", "test_topic_bh_6" ,"--host","http://localhost:50091", "-b" ,"1561112964"};
        final TopicArg topicArg=new TopicArg();
        final PublishArg publishArg=new PublishArg();
        final SubscribeArg consumeArg=new SubscribeArg();
        final PartitionGroupArg partitionGroupArg=new PartitionGroupArg();
        TopicAdmin topicAdmin=new TopicAdmin();
        Map<String,CommandArgs> argsMap=new HashMap(8);
        argsMap.put(Command.add.name(),topicArg);
        argsMap.put(Command.publish.name(),publishArg);
        argsMap.put(Command.subscribe.name(),consumeArg);
        JCommander jc =JCommander.newBuilder()
                .addObject(topicAdmin)
                .addCommand(Command.add.name(),topicArg)
                .addCommand(Command.delete.name(),topicAdmin)
                .addCommand(Command.publish.name(),publishArg)
                .addCommand(Command.unpublish.name(),publishArg)
                .addCommand(Command.subscribe.name(),consumeArg)
                .addCommand(Command.unsubscribe.name(),consumeArg)
                .addCommand(Command.partitiongroup.name(),partitionGroupArg)
                .build();
        jc.setProgramName("topic");
        topicAdmin.execute(jc,argV,argsMap);
    }

    /**
     *  Process  commands
     *
     **/
    public void process(String command,CommandArgs arguments,JCommander jCommander) throws Exception{
        Command type=Command.type(command);
        switch (type){
            case add:
                add((TopicArg) arguments,jCommander);
                break;
            case delete:
                delete((TopicArg) arguments,jCommander);
                break;
            case update:
                break;
            case publish:
                publish((PublishArg) arguments,jCommander);
                break;
            case unpublish:
                unPublish((PublishArg)arguments,jCommander);
                break;
            case subscribe:
                subscribe((SubscribeArg) arguments,jCommander);
                break;
            case unsubscribe:
                unSubscribe((SubscribeArg)arguments,jCommander);
                break;
            case partitiongroup:
                partitionGroups((PartitionGroupArg)arguments,jCommander);
                break;
            default:
                jCommander.usage();
                System.exit(-1);
                break;
        }
    }

    /**
     *
     *  Topic partition group
     *
     **/
    public String partitionGroups(PartitionGroupArg args,JCommander jCommander) throws Exception{
        PartitionGroupQuery query=new PartitionGroupQuery();
        query.setTopic(args.topic);
        query.setNamespace(args.namespace);
        Future<String> futureResult=httpClient.post(args.host,"/partitiongroup/list",JSON.toJSONString(query),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        logger.info("partition groups:{}",result);
        return result;
    }
    /**
     *  Topic add process
     *
     **/
    public  String add(TopicArg arguments,JCommander jCommander) throws Exception{
        List<PartitionGroup> partitionGroups;
        Topic topic=new Topic();
        topic.setType(Topic.Type.valueOf(arguments.type.byteValue()));
        topic.setName(new TopicName(arguments.code,arguments.namespace));
        topic.setPartitions(arguments.partitions.shortValue());
        if(arguments.partitionGroup!=null&&arguments.partitionGroup.size()>0) {
            partitionGroups = arguments.partitionGroup.stream().map(partitionGroup -> JSON.parseObject(partitionGroup, PartitionGroup.class)).collect(Collectors.toList());
        }else{
            partitionGroups=new ArrayList<>();
            PartitionGroup partitionGroup=new PartitionGroup();
            partitionGroup.setTopic(topic.getName());
            Set<Short> partitions=new HashSet();
            for(short i=0;i<arguments.partitions;i++){
                partitions.add(i);
            }
            partitionGroup.setPartitions(partitions);
            partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(arguments.election));
            Set<Integer> brokerIds=new HashSet<>(arguments.brokers);
            partitionGroup.setReplicas(brokerIds);
            partitionGroup.setRecLeader(arguments.brokers.get(0));
            partitionGroups.add(partitionGroup);
        }
        JSONObject request = new JSONObject();
        request.put("topic",JSON.toJSONString(topic));
        request.put("partitionGroups",JSON.toJSONString(partitionGroups));
        Future<String> futureResult=httpClient.post(arguments.host,"/topic/add",request.toJSONString(),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        logger.info("create topic :{}",result);
        return result;
    }

    /**
     *  Topic delete
     *
     **/
    public  String delete(TopicArg arguments,JCommander jCommander) throws Exception{
        Topic topic=new Topic();
        topic.setName(new TopicName(arguments.code,arguments.namespace));
        Future<String> futureResult=httpClient.post(arguments.host,"/topic/remove",JSON.toJSONString(topic),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        logger.info("delete topic {}:{}",arguments.code,result);
        System.out.println(result);
        return result;
    }


    /**
     * Publish to a topic
     *
     **/
    public  String publish(PublishArg pubSubArg,JCommander jCommander) throws Exception{
        Producer producer=new Producer();
        producer.setApp(pubSubArg.subscribe.app);
        producer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        producer.setType(Subscription.Type.valueOf(pubSubArg.subscribe.type.byteValue()));
        producer.setClientType(ClientType.valueOf(pubSubArg.subscribe.client));
        Producer.ProducerPolicy producerPolicy=Producer.ProducerPolicy.Builder.build().archive(pubSubArg.producerPolicy.archive)
                .blackList(pubSubArg.producerPolicy.blackList)
                .nearby(pubSubArg.producerPolicy.nearby)
                .single(pubSubArg.producerPolicy.single)
                .timeout(pubSubArg.producerPolicy.timeout)
                .weight(pubSubArg.producerPolicy.weight)
                .archive(pubSubArg.producerPolicy.archive).create();
        producer.setProducerPolicy(producerPolicy);
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/producer/add",JSON.toJSONString(producer),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        logger.info("publish topic {}, app {} {}",pubSubArg.subscribe.topic,pubSubArg.subscribe.app,result);
        return result;
    }

    /**
     * Unpublish to a topic
     *
     **/
    public  String unPublish(PublishArg pubSubArg,JCommander jCommander) throws Exception{
        Producer producer=new Producer();
        producer.setApp(pubSubArg.subscribe.app);
        producer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/producer/remove",JSON.toJSONString(producer),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        logger.info("unPublish topic {},app  {} {}",pubSubArg.subscribe.topic,pubSubArg.subscribe.app,result);
        return result;
    }



    /**
     *  Subscribe to a topic
     *
     **/
    public  String subscribe( SubscribeArg pubSubArg,JCommander jCommander) throws Exception{
        Consumer consumer=new Consumer();
        consumer.setApp(pubSubArg.subscribe.app);
        consumer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        consumer.setType(Subscription.Type.valueOf(pubSubArg.subscribe.type.byteValue()));
        consumer.setClientType(ClientType.valueOf(pubSubArg.subscribe.client));
        Consumer.ConsumerPolicy  consumerPolicy= Consumer.ConsumerPolicy.Builder.build()
                .ackTimeout(pubSubArg.consumerPolicyArg.ackTimeout)
                .paused(pubSubArg.consumerPolicyArg.paused)
                .archive(pubSubArg.consumerPolicyArg.archive)
                .blackList(pubSubArg.consumerPolicyArg.blackList)
                .delay(pubSubArg.consumerPolicyArg.delay)
                .batchSize(pubSubArg.consumerPolicyArg.batchSize==null?null:pubSubArg.consumerPolicyArg.batchSize.shortValue())
                .nearby(pubSubArg.consumerPolicyArg.nearby)
                .concurrent(pubSubArg.consumerPolicyArg.concurrent)
                .retry(pubSubArg.consumerPolicyArg.retry)
                .create();
        consumer.setConsumerPolicy(consumerPolicy);
        String consumerJson=JSON.toJSONString(consumer);
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/consumer/add",consumerJson,String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        logger.info("subscribe topic {},app {} {}",pubSubArg.subscribe.topic,pubSubArg.subscribe.app,result);
        return result;
    }

    /**
     *  Subscribe to a topic
     *
     **/
    public  String unSubscribe(SubscribeArg pubSubArg,JCommander jCommander) throws Exception{
        Consumer consumer=new Consumer();
        consumer.setApp(pubSubArg.subscribe.app);
        consumer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        String consumerJson=JSON.toJSONString(consumer);
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/consumer/remove",consumerJson,String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        logger.info("unSubscribe topic {},app {} {}",pubSubArg.subscribe.topic,pubSubArg.subscribe.app,result);
        return result;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    enum Command{
        add,delete,update,publish,unpublish,subscribe,unsubscribe,partitiongroup,undef;
        public static Command type(String name){
            for(Command c: values()){
                if(c.name().equals(name))
                    return c;
            }
            return undef;
        }
    }
}
