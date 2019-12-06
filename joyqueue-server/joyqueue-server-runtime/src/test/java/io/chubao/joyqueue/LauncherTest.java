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

import io.chubao.joyqueue.broker.Launcher;
import io.chubao.joyqueue.broker.config.Args;
import io.chubao.joyqueue.broker.config.ConfigDef;
import io.chubao.joyqueue.toolkit.network.IpUtil;
import io.chubao.joyqueue.tools.launch.JavaProcessLauncher;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Instance Launcher
 **/
public class LauncherTest {

    private String DEFAULT_CONFIG="joyqueue.properties";
    private String ROOT_DIR ="/tmp/joyqueue";
    @Test
    public void launchSecondBroker() throws  Exception{
        String dataDir= ROOT_DIR+"/second/Data";
        String log= ROOT_DIR+"/second/Logs/info.log";
        String journalKeeperNodes = String.format("%s", IpUtil.getLocalIp()+":"+String.valueOf(60088+6));
        FutureTask<JavaProcessLauncher> launcher=launchBroker(DEFAULT_CONFIG,60088,dataDir,log,journalKeeperNodes);
        launcher.get().destroy();
        System.out.println("destroy launcher");

    }


    @Test
    public void launchMultiBroker() throws  Exception{
        String dataDir= ROOT_DIR+"/first/Data";
        String log= ROOT_DIR+"/first/Logs/info.log";
        FutureTask<JavaProcessLauncher> firstBroker=launchBroker(DEFAULT_CONFIG,50088,dataDir,log,null);
        Thread.sleep(10);

         dataDir= ROOT_DIR+"/second/Data";
         log= ROOT_DIR+"/second/Logs/info.log";

        FutureTask<JavaProcessLauncher> secondBroker=launchBroker(DEFAULT_CONFIG,60088,dataDir,log,null);

        firstBroker.get().destroy();
        secondBroker.get().destroy();
        System.out.println("destroy all processes");
    }


    @Test
    public void launchClusterBroker() throws Exception{
        int firstPort=40088;
        int secondPort=50088;
        int thirdPort= 60088;
        String journalKeeperNodes = String.format("%s,%s,%s",IpUtil.getLocalIp()+":"+String.valueOf(firstPort+6),
                IpUtil.getLocalIp()+":"+String.valueOf(secondPort+6),IpUtil.getLocalIp()+":"+String.valueOf(thirdPort+6));

        String dataDir= ROOT_DIR+"/first/Data";
        String log= ROOT_DIR+"/first/Logs/info.log";


        FutureTask<JavaProcessLauncher> firstBroker=launchBroker(DEFAULT_CONFIG,firstPort,dataDir,log,journalKeeperNodes);
        Thread.sleep(10);

        dataDir= ROOT_DIR+"/second/Data";
        log= ROOT_DIR+"/second/Logs/info.log";


        FutureTask<JavaProcessLauncher> secondBroker=launchBroker(DEFAULT_CONFIG,secondPort,dataDir,log,journalKeeperNodes);

        dataDir= ROOT_DIR+"/third/Data";
        log= ROOT_DIR+"/third/Logs/info.log";
        FutureTask<JavaProcessLauncher> thirdBroker=launchBroker(DEFAULT_CONFIG,thirdPort,dataDir,log,journalKeeperNodes);

        Thread.sleep(TimeUnit.MINUTES.toMillis(10));

        firstBroker.get().destroy();
        secondBroker.get().destroy();
        thirdBroker.get().destroy();
        System.out.println("destroy all processes");

    }


    /**
     * Launcher broker process with config
     *
     **/
    public FutureTask<JavaProcessLauncher> launchBroker(String configFile, int port, String storePath,
                                            String logFile,String journalKeeperNodes) throws Exception{
        Args args=new Args();
        args.append(ConfigDef.APPLICATION_DATA_PATH.key(),storePath);
        args.append(ConfigDef.TRANSPORT_SERVER_PORT.key(),String.valueOf(port));
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
        FutureTask<JavaProcessLauncher> futureTask=new FutureTask(()->{
            JavaProcessLauncher launcher = new JavaProcessLauncher(Launcher.class, finalArgs, logFile, "JoyQueue is started");
            try {
                launcher.start();
                launcher.waitForReady(60, TimeUnit.SECONDS);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            return launcher;
        });
        Thread thread=new Thread(futureTask);
        thread.start();
        //return  futureTask.get;
        return futureTask;
    }

    @After
    public void cleanup(){
        File root=new File(ROOT_DIR);
        if(root.exists()){
           // root.listFiles()
            root.delete();
        }
    }
}
