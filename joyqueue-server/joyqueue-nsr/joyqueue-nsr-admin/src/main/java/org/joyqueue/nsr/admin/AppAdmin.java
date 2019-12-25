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
import org.joyqueue.domain.AppToken;
import org.joyqueue.nsr.AdminConfig;
import org.joyqueue.nsr.CommandArgs;
import org.joyqueue.nsr.model.AppTokenQuery;
import org.joyqueue.nsr.utils.AsyncHttpClient;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AppAdmin extends AbstractAdmin {
    private  static final Logger logger= LoggerFactory.getLogger(AppAdmin.class);
    private AsyncHttpClient httpClient;
    public AppAdmin(){
        this(new AsyncHttpClient());
    }
    public AppAdmin(AsyncHttpClient httpClient){
        this.httpClient=httpClient;
    }
    @Parameters(separators = "=", commandDescription = "Generate a token for App")
    public static class TokenArg extends CommandArgs {
        private  static final Long MONTH_MS=86400000L;

        @Parameter(names = { "-a", "--app" }, description = "App code", required = true)
        public String app;

        @Parameter(names = { "-s", "--start" }, description = "When to be effective,default now", required = false)
        public Long start=SystemClock.now();

        @Parameter(names = { "-e", "--expire" }, description = "Expire time, default expire after 1 year ", required = false)
        public Long expire=SystemClock.now()+MONTH_MS*12;
    }

    @Parameters(separators = "=", commandDescription = "List a token for App")
    public static class TokensArg extends CommandArgs {

        @Parameter(names = { "-a", "--app" }, description = "App code", required = true)
        public String app;

        @Parameter(names = { "-t", "--token" }, description = "App token", required = false)
        public String token;

    }


    public static void main(String[] args){
        final TokenArg tokenArg=new TokenArg();
        final TokensArg tokensArg=new TokensArg();
        String[] argv={"token","--host","http://localhost:50091","-a","test_app"};
        AppAdmin appAdmin=new AppAdmin();
        Map<String,CommandArgs> argsMap=new HashMap(8);
        argsMap.put(Command.token.name(),tokenArg);
        argsMap.put(Command.list.name(),tokensArg);
        JCommander jc =JCommander.newBuilder()
                .addObject(appAdmin)
                .addCommand(Command.token.name(),tokenArg)
                .addCommand(Command.list.name(),tokensArg)
                .build();
        jc.setProgramName("app");
        appAdmin.execute(jc,argv,argsMap);
    }


    public void process(String command, CommandArgs arguments, JCommander jCommander) throws Exception {
        Command type=Command.type(command);
        switch (type){
            case token:
                token((TokenArg)arguments,jCommander);
                break;
            case list:
                tokens((TokensArg) arguments,jCommander);
            default:
                jCommander.usage();
                System.exit(-1);
                break;
        }
    }



    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    /**
     *  Create a token for app
     *
     **/
    public  String token(TokenArg arguments,JCommander jCommander) throws Exception{
        AppToken token=new AppToken();
        token.setId(SystemClock.now());
        token.setApp(arguments.app);
        token.setEffectiveTime(new Date(arguments.start));
        token.setExpirationTime(new Date(arguments.expire));
        token.setToken(UUID.randomUUID().toString().replaceAll("-" , ""));
        Future<String> futureResult=httpClient.post(arguments.host,"/apptoken/add",JSON.toJSONString(token),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        if(result!=null&&result.equals("success")){
            result=token.getToken();
        }
        System.out.println(result);
        logger.info("token:{}",result);
        return result;
    }


    /**
     *  List tokens of app
     *
     **/
    public List<AppToken> tokens(TokensArg arguments, JCommander jCommander) throws Exception{
        AppTokenQuery tokenQuery=new AppTokenQuery();
        List<AppToken> tokens=null;
        tokenQuery.setApp(arguments.app);
        tokenQuery.setToken(arguments.token);
        Future<String> futureResult=httpClient.post(arguments.host,"/apptoken/list",JSON.toJSONString(tokenQuery),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        if(result!=null){
            tokens= JSON.parseArray(result,AppToken.class);
        }
        if(tokens==null) tokens=new ArrayList();
        System.out.println(result);
        logger.info("tokens:{}",result);
        return tokens;
    }

    /**
     * Command type enum
     *
     **/
    enum Command{
        token,list,undef;
        public static Command type(String name){
            for(Command c: values()){
                if(c.name().equals(name))
                    return c;
            }
            return undef;
        }
    }
}
