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
package io.chubao.joyqueue;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.broker.Launcher;
import io.chubao.joyqueue.broker.config.Args;
import io.chubao.joyqueue.broker.config.ConfigDef;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.monitor.RestResponse;
import io.chubao.joyqueue.monitor.RestResponseCode;
import io.chubao.joyqueue.toolkit.URL;
import io.chubao.joyqueue.toolkit.io.Files;
import io.chubao.joyqueue.toolkit.network.IpUtil;
import io.chubao.joyqueue.toolkit.network.http.Get;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import io.chubao.joyqueue.tools.launch.JavaProcessLauncher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Launch multiple MQ nodes on single machine
 *
 **/
public class LauncherTest {

    private String DEFAULT_CONFIG="joyqueue.properties";
    private String DEFAULT_JOYQUEUE="joyqueue";
    private String ROOT_DIR =System.getProperty("java.io.tmpdir")+DEFAULT_JOYQUEUE;

    public void makeSureDirectoryExist(String path){
        File root=new File(path);
        if(!root.exists()){
            root.mkdirs();
        }
    }
    @Test
    public void launchOneBroker(){
        String dataDir= ROOT_DIR+"/second/Data";
        makeSureDirectoryExist(dataDir);
        Broker broker=new Broker();
        broker.setPort(60088);
        String journalKeeperNodes = String.format("%s", IpUtil.getLocalIp()+":"+String.valueOf(broker.getJournalkeeperPort()));
        FutureTask<JavaProcessLauncher> launcher=launchBroker(DEFAULT_CONFIG,broker,dataDir,journalKeeperNodes);
        long timeout=60;
        TimeUnit unit= TimeUnit.SECONDS;
        boolean launchSuccess=waitBrokerStart(launcher,timeout,unit);
        Assert.assertTrue(launchSuccess);



    }


    @Test
    public void launchMultiBroker() throws  Exception{
        String dataDir= ROOT_DIR+"/first/Data";
        makeSureDirectoryExist(dataDir);
        Broker firstPort=new Broker();
        firstPort.setPort(40088);
        FutureTask<JavaProcessLauncher> firstBroker=launchBroker(DEFAULT_CONFIG,firstPort,dataDir,null);
        Thread.sleep(10);


        Broker secondPort=new Broker();
        secondPort.setPort(50088);
        dataDir= ROOT_DIR+"/second/Data";
        makeSureDirectoryExist(dataDir);
        FutureTask<JavaProcessLauncher> secondBroker=launchBroker(DEFAULT_CONFIG,secondPort,dataDir,null);

        Broker thirdPort=new Broker();
        thirdPort.setPort(60088);
        dataDir= ROOT_DIR+"/third/Data";
        makeSureDirectoryExist(dataDir);
        FutureTask<JavaProcessLauncher> thirdBroker=launchBroker(DEFAULT_CONFIG,thirdPort,dataDir,null);
        long timeout=60;
        TimeUnit unit= TimeUnit.SECONDS;
        boolean allBrokerStart=waitBrokerStart(firstBroker,timeout,unit);
                allBrokerStart=waitBrokerStart(secondBroker,timeout,unit)&&allBrokerStart;
                allBrokerStart=waitBrokerStart(thirdBroker,timeout,unit)&&allBrokerStart;
        Assert.assertTrue(allBrokerStart);
    }

    @Test
    public void launchClusterBroker() throws Exception{
        Broker firstPort=new Broker();
        firstPort.setPort(40088);

        Broker secondPort=new Broker();
        secondPort.setPort(50088);

        Broker thirdPort=new Broker();
        thirdPort.setPort(60088);

        String journalKeeperNodes = String.format("%s,%s,%s",IpUtil.getLocalIp()+":"+String.valueOf(firstPort.getJournalkeeperPort()),
                IpUtil.getLocalIp()+":"+String.valueOf(secondPort.getJournalkeeperPort()),IpUtil.getLocalIp()+":"+String.valueOf(thirdPort.getJournalkeeperPort()));

        String dataDir= ROOT_DIR+"/first/Data";
        makeSureDirectoryExist(dataDir);

        FutureTask<JavaProcessLauncher> firstBroker=launchBroker(DEFAULT_CONFIG,firstPort,dataDir,journalKeeperNodes);

        dataDir= ROOT_DIR+"/second/Data";
        makeSureDirectoryExist(dataDir);

        FutureTask<JavaProcessLauncher> secondBroker=launchBroker(DEFAULT_CONFIG,secondPort,dataDir,journalKeeperNodes);

        dataDir= ROOT_DIR+"/third/Data";
        makeSureDirectoryExist(dataDir);

        FutureTask<JavaProcessLauncher> thirdBroker=launchBroker(DEFAULT_CONFIG,thirdPort,dataDir,journalKeeperNodes);

        // mock 2 minutes test logic
        // make sure release all process
        long timeout=60;
        TimeUnit unit= TimeUnit.SECONDS;
        boolean clusterStartSuccessful=waitBrokerStart(firstBroker,timeout,unit);
        clusterStartSuccessful=waitBrokerStart(secondBroker,timeout,unit)&&clusterStartSuccessful;
        clusterStartSuccessful=waitBrokerStart(thirdBroker,timeout,unit)&&clusterStartSuccessful;
        System.out.println("destroy all processes");
        Assert.assertTrue(clusterStartSuccessful);

    }

    /**
     *  Wait broker start success or timeout
     **/
    public boolean waitBrokerStart( FutureTask<JavaProcessLauncher> broker,long timeout,TimeUnit unit){
        boolean startSuccessful=false;
        try {
            broker.get(timeout, unit).destroy();
            startSuccessful = true;
        }catch (Exception e){
            System.out.println(e.getMessage());

        }
        return startSuccessful;
    }


    /**
     * Launcher broker process with config
     *
     **/
    public FutureTask<JavaProcessLauncher> launchBroker(String configFile, Broker broker, String storePath,
                                            String journalKeeperNodes){
        Args args=new Args();
        args.append(ConfigDef.APPLICATION_DATA_PATH.key(),storePath);
        args.append(ConfigDef.TRANSPORT_SERVER_PORT.key(),String.valueOf(broker.getPort()));
        if(journalKeeperNodes!=null) {
            args.append(ConfigDef.NAME_SERVER_JOURNAL_KEEPER_NODES.key(), journalKeeperNodes);
        }
        String[] argPairs= args.build();
        String[] finalArgs;
        if(argPairs.length>0){
            finalArgs= new String[argPairs.length+1];
            finalArgs[0]= configFile;
            System.arraycopy(argPairs,0,finalArgs,1,argPairs.length);
        }else{
            finalArgs =new String[1];
            finalArgs[0]= configFile;
        }
        String  localIp= IpUtil.getLocalIp();
        FutureTask<JavaProcessLauncher> futureTask=new FutureTask(()->{
            JavaProcessLauncher launcher = new JavaProcessLauncher(Launcher.class, finalArgs);
            launcher.start(String.valueOf(broker.getPort()));
            try {
                waitBrokerReady(localIp, broker.getMonitorPort(), 60, TimeUnit.SECONDS);
                return launcher;
            }catch (Exception e){
                launcher.destroy();
                throw  e;
            }

        });
        Thread thread=new Thread(futureTask);
        thread.start();
        return futureTask;
    }

    /**
     *
     * @return true if broker ready
     * @throws TimeoutException
     *
     **/
    public boolean waitBrokerReady(String host,int port,long timeout,TimeUnit unit) throws Exception {
        URL url= URL.valueOf(String.format("http://%s:%s/started",host,port));
        Get  http= Get.Builder.build().connectionTimeout((int)TimeUnit.MILLISECONDS.toMillis(30))
                .socketTimeout((int)TimeUnit.MILLISECONDS.toMillis(10)).create();
        long timeoutMs= SystemClock.now()+unit.toMillis(timeout);
        do{
            try {
                Thread.sleep(1000);
                String startSign = http.get(url);
                RestResponse<Boolean> restResponse=JSON.parseObject(startSign, RestResponse.class);
                if (restResponse != null&& restResponse.getCode()== RestResponseCode.SUCCESS.getCode() &&restResponse.getData()!=null&&restResponse.getData()) {
                    return true;
                }

            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }while(timeoutMs>SystemClock.now());
        throw  new TimeoutException("wait for broker ready timeout! ");
    }

    @After
    public void cleanup(){
        System.out.print("clean up");
        File root=new File(ROOT_DIR);
        if(root.exists()){
            Files.deleteDirectory(root);
        }
    }
}
