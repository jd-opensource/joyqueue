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
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.joyqueue.domain.Broker;
import org.joyqueue.nsr.AdminConfig;
import org.joyqueue.nsr.CommandArgs;
import org.joyqueue.nsr.model.BrokerQuery;
import org.joyqueue.nsr.utils.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BrokerAdmin extends AbstractAdmin {
    private  static final  Logger logger= LoggerFactory.getLogger(BrokerAdmin.class);
    private AsyncHttpClient httpClient;
    public BrokerAdmin(){
        this(new AsyncHttpClient());
    }
    public BrokerAdmin(AsyncHttpClient httpClient){
        this.httpClient=httpClient;
    }
    @Parameters(separators = "=", commandDescription = "List broker arguments")
    public static class ListArg extends CommandArgs {

        @Parameter(names = { "-i", "--id" }, description = "broker id", required = false)
        public int id;

        @Parameter(names = { "--ip" }, description = "broker ip", required = false)
        public String ip;

        @Parameter(names = { "--key" }, description = "broker query keyword ", required = false)
        public String key;
        @Parameter(names = { "-b", "--brokers" }, description = "brokers id list", required = false)
        public List<Integer> brokers=new ArrayList<>();
    }

    public static void main(String[] args){
        final ListArg listArg=new ListArg();
        //String[] argv={"list","--host","http://localhost:50091"};
        BrokerAdmin brokerAdmin=new BrokerAdmin();
        Map<String,CommandArgs> argsMap=new HashMap(8);
        argsMap.put(Command.list.name(),listArg);
        JCommander jc =JCommander.newBuilder()
                .addObject(brokerAdmin)
                .addCommand(Command.list.name(),listArg)
                .build();
        jc.setProgramName("broker");
        brokerAdmin.execute(jc,args,argsMap);
    }


    /**
     *  Process  commands
     *
     **/
    public  void process(String command, CommandArgs arguments, JCommander jCommander) throws Exception{
        Command type=Command.type(command);
        switch (type){
            case list:
                list(arguments,jCommander);
                break;
            default:
                jCommander.usage();
                System.exit(-1);
                break;
        }
    }

    /**
     *  List available brokers
     *
     **/
    public  List<Broker> list(CommandArgs commandArgs,JCommander jCommander) throws Exception{
        ListArg arguments=null;
        if(commandArgs instanceof ListArg){
            arguments=(ListArg)commandArgs;
        }else{
            throw new IllegalArgumentException("bad args");
        }
        BrokerQuery brokerQuery=new BrokerQuery();
        brokerQuery.setIp(arguments.ip);
        brokerQuery.setBrokerId(arguments.id);
        brokerQuery.setBrokerList(arguments.brokers);
        brokerQuery.setKeyword(arguments.key);
        Future<String> futureResult=httpClient.post(arguments.host,"/broker/list",JSON.toJSONString(brokerQuery),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        List<Broker> brokers=null;
        if(result!=null){
            brokers =JSON.parseArray(result,Broker.class);
        }
        if(brokers!=null){
            logger.info(result);
        }
        return brokers;
    }
    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    enum Command{
        list,undef;
        public static Command type(String name){
            for(Command c: values()){
                if(c.name().equals(name))
                    return c;
            }
            return undef;
        }
    }


}
