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
package com.jd.journalq.nsr.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.Subscription;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.nsr.AdminConfig;
import com.jd.journalq.nsr.CommandArgs;
import com.jd.journalq.nsr.utils.AsyncHttpClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TopicAdmin extends AbstractAdmin {

    private AsyncHttpClient httpClient;
    public TopicAdmin(){
        this.httpClient=new AsyncHttpClient();
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

    /**
     *  Subscription args
     *
     **/
    public static class SubscribeArg {
        @Parameter(names = { "-c", "--topic" }, description = "Topic code", required = true)
        public String topic;

        @Parameter(names = { "-a", "--app" }, description = "Topic code", required = true)
        public String app;

        @Parameter(names = { "-n", "--namespace" }, description = "Topic namespace", required = false)
        public String namespace="";

        @Parameter(names = { "-t", "--type" }, description = "Topic type:1 produce,2 consume", required = true)
        public Integer type;
    }

    @Parameters(separators = "=", commandDescription = "Publish or subscribe args")
    public static class PubSubArg extends CommandArgs{
        @Parameter(names = { "--host" }, description = " Naming address", required = false)
        public String host="http://localhost:50091";
        @ParametersDelegate
        public SubscribeArg subscribe=new SubscribeArg();
        @Parameter(names = {  "--client" }, description = "client type: 0 jmq,1 kafka,2 mqtt,other  ", required = false)
        public Integer client=0;

    }
    //192.168.73.147 1543486432  1543486547 "-b" ,"1543486432","-b","1543486547"
    // local 1556178070
    String[] argV={"add", "-c", "test_topic_bh_6" ,"--host","http://localhost:50091", "-b" ,"1556178070"};
    //String[] argV={"publish", "--topic", "test_topic_bh_6" ,"--app","test_app","--type","1","--host","http://localhost:50091"};

    public static void main(String[] args){
        final TopicArg topicArg=new TopicArg();
        final PubSubArg pubSubArg=new PubSubArg();
        TopicAdmin topicAdmin=new TopicAdmin();
        Map<String,CommandArgs> argsMap=new HashMap(8);
                                argsMap.put(Command.add.name(),topicArg);
                                argsMap.put(Command.publish.name(),pubSubArg);
                                argsMap.put(Command.subscribe.name(),pubSubArg);
        JCommander jc =JCommander.newBuilder()
                .addObject(topicAdmin)
                .addCommand(Command.add.name(),topicArg)
                .addCommand(Command.delete.name(),topicAdmin)
                .addCommand(Command.publish.name(),pubSubArg)
                .addCommand(Command.unpublish.name(),pubSubArg)
                .addCommand(Command.subscribe.name(),pubSubArg)
                .addCommand(Command.unsubscribe.name(),pubSubArg)
                .build();
        jc.setProgramName("topic");
        topicAdmin.execute(jc,args,argsMap);
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
               publish((PubSubArg)arguments,jCommander);
               break;
           case unpublish:
               unPublish((PubSubArg)arguments,jCommander);
               break;
           case subscribe:
               subscribe((PubSubArg)arguments,jCommander);
               break;
           case unsubscribe:
               unSubscribe((PubSubArg)arguments,jCommander);
               break;
           default:
               jCommander.usage();
               System.exit(-1);
               break;
       }
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
        return result;
    }

    /**
     *  Topic delete
     *
     **/
    public  String delete(TopicArg arguments,JCommander jCommander) throws Exception{
        List<PartitionGroup> partitionGroups;
        Topic topic=new Topic();
        topic.setName(new TopicName(arguments.code,arguments.namespace));
        Future<String> futureResult=httpClient.post(arguments.host,"/topic/remove",JSON.toJSONString(topic),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }


    /**
     * Publish to a topic
     *
     **/
    public  String publish(PubSubArg pubSubArg,JCommander jCommander) throws Exception{
        Producer producer=new Producer();
        producer.setApp(pubSubArg.subscribe.app);
        producer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        producer.setType(Subscription.Type.valueOf(pubSubArg.subscribe.type.byteValue()));
        producer.setClientType(ClientType.valueOf(pubSubArg.client));
        producer.setProducerPolicy(new Producer.ProducerPolicy());
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/producer/add",JSON.toJSONString(producer),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }

    /**
     * Unpublish to a topic
     *
     **/
    public  String unPublish(PubSubArg pubSubArg,JCommander jCommander) throws Exception{
        Producer producer=new Producer();
        producer.setApp(pubSubArg.subscribe.app);
        producer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/producer/remove",JSON.toJSONString(producer),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }



    /**
     *  Subscribe to a topic
     *
     **/
    public  String subscribe(PubSubArg pubSubArg,JCommander jCommander) throws Exception{
        Consumer consumer=new Consumer();
        consumer.setApp(pubSubArg.subscribe.app);
        consumer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        consumer.setType(Subscription.Type.valueOf(pubSubArg.subscribe.type.byteValue()));
        consumer.setClientType(ClientType.valueOf(pubSubArg.client));
        String consumerJson=JSON.toJSONString(consumer);
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/consumer/add",consumerJson,String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }

    /**
     *  Subscribe to a topic
     *
     **/
    public  String unSubscribe(PubSubArg pubSubArg,JCommander jCommander) throws Exception{
        Consumer consumer=new Consumer();
        consumer.setApp(pubSubArg.subscribe.app);
        consumer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        String consumerJson=JSON.toJSONString(consumer);
        Future<String> futureResult=httpClient.post(pubSubArg.host,"/consumer/remove",consumerJson,String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    enum Command{
       add,delete,update,publish,unpublish,subscribe,unsubscribe,undef;
       public static Command type(String name){
           for(Command c: values()){
               if(c.name().equals(name))
                   return c;
           }
           return undef;
       }
    }
}
