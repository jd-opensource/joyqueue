package io.chubao.joyqueue;

import io.chubao.joyqueue.broker.Launcher;
import io.chubao.joyqueue.broker.config.Args;
import io.chubao.joyqueue.broker.config.ConfigDef;
import io.chubao.joyqueue.tools.launch.JavaProcessLauncher;
import org.junit.After;
import org.junit.Test;

import java.io.File;
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
        String cacheDir =ROOT_DIR+"/second/cache";
        String journalKeeperDir= ROOT_DIR+"/second/jk";
        JavaProcessLauncher launcher=launchBroker(DEFAULT_CONFIG,60088,60093,cacheDir,
                                                    journalKeeperDir, 60095,dataDir,log);
        launcher.destroy();
        System.out.println("destroy launcher");

    }


    @Test
    public void launchMultiBroker() throws  Exception{
        String dataDir= ROOT_DIR+"/first/Data";
        String log= ROOT_DIR+"/first/Logs/info.log";
        String cacheDir =ROOT_DIR+"/fisrt/cache";
        String journalKeeperDir= ROOT_DIR+"/first/jk";

        JavaProcessLauncher firstBroker=launchBroker(DEFAULT_CONFIG,50088,50093,cacheDir,
                                                    journalKeeperDir,50095,dataDir,log);
        Thread.sleep(10);

         dataDir= ROOT_DIR+"/second/Data";
         log= ROOT_DIR+"/second/Logs/info.log";
         cacheDir =ROOT_DIR+"/second/cache";
         journalKeeperDir= ROOT_DIR+"/second/jk";

        JavaProcessLauncher secondBroker=launchBroker(DEFAULT_CONFIG,60088,60093,cacheDir,
                                                        journalKeeperDir, 60095,dataDir,log);

        firstBroker.destroy();
        secondBroker.destroy();
        System.out.println("destroy all processes");
    }


    /**
     * Launcher broker process with config
     *
     **/
    public JavaProcessLauncher launchBroker(String configFile, int port, int nameServiceMessagerPort, String nameServiceCachePath,
                                            String nameServerJournalKeeperWorkingDir, int nameServerJournalKeeperPort, String storePath, String logFile) throws Exception{
        Args args=new Args();
        args.append(ConfigDef.APPLICATION_DATA_PATH.key(),storePath);
        args.append(ConfigDef.TRANSPORT_SERVER_PORT.key(),String.valueOf(port));
        args.append(ConfigDef.NAME_SERVICE_CACHE_PATH.key(),nameServiceCachePath);
        args.append(ConfigDef.NAME_SERVICE_MESSAGE_PORT.key(),String.valueOf(nameServiceMessagerPort));
        args.append(ConfigDef.NAME_SERVER_JOURNALKEEPER_PORT.key(),String.valueOf(nameServerJournalKeeperPort));
        args.append(ConfigDef.NAME_SERVER_JOURNALKEEPER_WORKING_DIR.key(),nameServerJournalKeeperWorkingDir);
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
        JavaProcessLauncher launcher=new JavaProcessLauncher(Launcher.class,finalArgs,logFile,"JoyQueue is started");
        launcher.start();
        launcher.waitForReady(60,TimeUnit.SECONDS);
        return  launcher;
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
