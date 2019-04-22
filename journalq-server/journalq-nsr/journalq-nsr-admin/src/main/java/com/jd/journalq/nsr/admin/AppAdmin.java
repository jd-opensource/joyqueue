package com.jd.journalq.nsr.admin;

import com.alibaba.fastjson.JSON;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.jd.journalq.domain.AppToken;
import com.jd.journalq.nsr.AdminConfig;
import com.jd.journalq.nsr.CommandArgs;
import com.jd.journalq.nsr.utils.AsyncHttpClient;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AppAdmin extends AbstractAdmin {
    @Parameters(separators = "=", commandDescription = "Generate a token for App")
    public static class TokenArg extends CommandArgs {
        private final static Long MONTH_MS=86400000L;
        @Parameter(names = { "--host" }, description = "Naming address", required = false)
        public String host="http://localhost:50091";

        @Parameter(names = { "-a", "--app" }, description = "Topic code", required = true)
        public String app;

        @Parameter(names = { "-s", "--start" }, description = "When to be effective,default now", required = false)
        public Long start=System.currentTimeMillis();

        @Parameter(names = { "-e", "--expire" }, description = "Expire time, default expire after 1 year ", required = false)
        public Long expire=System.currentTimeMillis()+MONTH_MS*12;
    }

    public static void main(String[] args){
        final TokenArg tokenArg=new TokenArg();
        //String[] argv={"token","--host","http://localhost:50091","-a","test_app"};
        AppAdmin appAdmin=new AppAdmin();
        Map<String,CommandArgs> argsMap=new HashMap(8);
                                argsMap.put(Command.token.name(),tokenArg);
        JCommander jc =JCommander.newBuilder()
                .addObject(appAdmin)
                .addCommand(Command.token.name(),tokenArg)
                .build();
        jc.setProgramName("broker");
        appAdmin.execute(jc,args,argsMap);
    }


    public void process(String command, CommandArgs arguments, JCommander jCommander) throws Exception {
        Command type=Command.type(command);
        switch (type){
            case token:
                token((TokenArg)arguments,jCommander);
                break;
            default:
                jCommander.usage();
                System.exit(-1);
                break;
        }
    }



    @Override
    public void close() throws IOException {
        AsyncHttpClient.close();
    }

    /**
     *  Create a token for app
     *
     **/
    public  String token(TokenArg arguments,JCommander jCommander) throws Exception{
        AppToken token=new AppToken();
        token.setId(System.currentTimeMillis());
        token.setApp(arguments.app);
        token.setEffectiveTime(new Date(arguments.start));
        token.setExpirationTime(new Date(arguments.expire));
        token.setToken(UUID.randomUUID().toString().replaceAll("-" , ""));
        Future<String> futureResult=AsyncHttpClient.post(arguments.host,"/apptoken/add",JSON.toJSONString(token),String.class);
        String result=futureResult.get(AdminConfig.TIMEOUT_MS,TimeUnit.MILLISECONDS);
        if(result!=null&&result.equals("success")){
            result=token.getToken();
        }
        System.out.println(result);
        return result;
    }

    /**
     * Command type enum
     *
     **/
    enum Command{
        token,undef;
        public static Command type(String name){
            for(Command c: values()){
                if(c.name().equals(name))
                    return c;
            }
            return undef;
        }
    }


}
