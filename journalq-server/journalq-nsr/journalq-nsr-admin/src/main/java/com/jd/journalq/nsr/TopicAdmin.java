package com.jd.journalq.nsr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.*;
import com.jd.journalq.domain.*;
import com.jd.journalq.nsr.impl.ProducerNameServerServiceImpl;
import com.jd.journalq.nsr.utils.AsyncHttpClient;
import com.jd.journalq.toolkit.retry.RetryPolicy;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TopicAdmin {

    @Parameters(separators = "=", commandDescription = "Topic arguments")
    static class TopicArg implements CommandArgs{
        @Parameter(names = {"-h", "--help"}, description = "Help message", help = true)
        public boolean help;

        @Parameter(names = { "--host" }, description = "Topic naming address", required = false)
        public String host="localhost:50091";
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
    static class SubscribeArg {
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
    static class PubSubArg implements CommandArgs{
        @Parameter(names = {"-h", "--help"}, description = "Help message", help = true)
        public boolean help;
        @Parameter(names = { "--host" }, description = "Topic naming address", required = false)
        public String host="localhost:50091";
        @ParametersDelegate
        SubscribeArg subscribe=new SubscribeArg();
        @Parameter(names = {  "--client" }, description = "client type: 0 jmq,1 kafka,2 mqtt,other  ", required = false)
        public Integer client=0;

    }

    @Parameter(names = {"-h", "--help"}, description = "Help message", help = true)
    public boolean help;

    public static void main(String[] args){
        //192.168.73.147 1543486432  1543486547 "-b" ,"1543486432","-b","1543486547"
        // local 1556178070
        //String[] argV={"add", "-c", "test_topic_bh_6" ,"--host","http://localhost:50091", "-b" ,"1556178070"};
        String[] argV={"publish", "--topic", "test_topic_bh_6" ,"--app","test_app","--type","1","--host","http://localhost:50091"};
        final TopicArg topicArg=new TopicArg();
        final PubSubArg pubSubArg=new PubSubArg();
        TopicAdmin topicAdmin=new TopicAdmin();
        Map<String,CommandArgs> argsMap=new HashMap(8);
                                argsMap.put(CommandType.add.name(),topicArg);
                                argsMap.put(CommandType.publish.name(),pubSubArg);
                                argsMap.put(CommandType.subscribe.name(),pubSubArg);
        JCommander jc =JCommander.newBuilder()
                .addObject(topicAdmin)
                .addCommand(CommandType.add.name(),topicArg)
                .addCommand(CommandType.update.name(),topicArg)
                .addCommand(CommandType.publish.name(),pubSubArg)
                .addCommand(CommandType.subscribe.name(),pubSubArg)
                .build();
        jc.setProgramName("topic");
        try {
            jc.parse(argV);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(-1);
        }
        if (topicAdmin.help) {
            jc.usage();
            System.exit(-1);
        }
        // command help
        if(topicArg.help||pubSubArg.help){
            jc.getCommands().get(jc.getParsedCommand()).usage();
            System.exit(-1);
        }
        try {
            String cmdName=jc.getParsedCommand();
            process(CommandType.type(jc.getParsedCommand()),argsMap.get(cmdName), jc);
        }catch (Exception e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        try {
            AsyncHttpClient.close();
        }catch (Exception e){
            System.out.print(e);
        }
    }


    /**
     * Show command usage
     **/
    static void commandHelp(JCommander jc){
        CommandType cmd=CommandType.type(jc.getParsedCommand());
        if(cmd==CommandType.undef){
            //System.err.println(jc);
            jc.usage();
        }else{
            jc.getCommands().get(cmd.name()).usage();
        }
    }


    private static  void process(CommandType type,CommandArgs arguments,JCommander jCommander) throws Exception{
       switch (type){
           case add:
               add(arguments,jCommander);
               break;
           case delete:
               break;
           case update:
               break;
           case publish:
               publish(arguments,jCommander);
               break;
           case subscribe:
               subscribe(arguments,jCommander);
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
    private static String add(CommandArgs commandArgs,JCommander jCommander) throws Exception{
        List<PartitionGroup> partitionGroups=null;
        TopicArg arguments=null;
        if(commandArgs instanceof TopicArg){
            arguments=(TopicArg)commandArgs;
        }else{
            throw new IllegalArgumentException("bad args");
        }
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
        Future<String> futureResult=AsyncHttpClient.post(arguments.host,"/topic/add",request.toJSONString(),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }


    /**
     * Publish to a topic
     *
     **/
    private static String publish(CommandArgs commandArgs,JCommander jCommander) throws Exception{
        PubSubArg pubSubArg=null;
        if(commandArgs instanceof PubSubArg){
            pubSubArg=(PubSubArg)commandArgs;
        }else{
            throw new IllegalArgumentException("bad args");
        }
        Producer producer=new Producer();
        producer.setApp(pubSubArg.subscribe.app);
        producer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        producer.setType(Subscription.Type.valueOf(pubSubArg.subscribe.type.byteValue()));
        producer.setClientType(ClientType.valueOf(pubSubArg.client));
        producer.setProducerPolicy(new Producer.ProducerPolicy());
        Future<String> futureResult=AsyncHttpClient.post(pubSubArg.host,"/producer/add",JSON.toJSONString(producer),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }

    /**
     * Publish to a topic
     *
     **/
    private static String subscribe(CommandArgs commandArgs,JCommander jCommander) throws Exception{
        PubSubArg pubSubArg=null;
        if(commandArgs instanceof PubSubArg){
            pubSubArg=(PubSubArg)commandArgs;
        }else{
            throw new IllegalArgumentException("bad args");
        }
        Consumer consumer=new Consumer();
        consumer.setApp(pubSubArg.subscribe.app);
        consumer.setTopic(new TopicName(pubSubArg.subscribe.topic,pubSubArg.subscribe.namespace));
        consumer.setType(Subscription.Type.valueOf(pubSubArg.subscribe.type.byteValue()));
        consumer.setClientType(ClientType.valueOf(pubSubArg.client));
        String consumerJson=JSON.toJSONString(consumer);
        Future<String> futureResult=AsyncHttpClient.post(pubSubArg.host,"/consumer/add",consumerJson,String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        System.out.println(result);
        return result;
    }


    enum CommandType{
       add,delete,update,publish,subscribe,undef;
       public static CommandType type(String name){
           for(CommandType c: values()){
               if(c.name().equals(name))
                   return c;
           }
           return undef;
       }
    }
}
