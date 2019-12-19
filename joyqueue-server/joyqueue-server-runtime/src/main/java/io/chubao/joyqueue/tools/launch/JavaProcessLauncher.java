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
package io.chubao.joyqueue.tools.launch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;


/**
 * Launcher a new process
 *
 **/
public class JavaProcessLauncher {
    protected static final Logger logger = LoggerFactory.getLogger(JavaProcessLauncher.class);
    private Class mainClass;
    private String[] args;
    private Process process;
    private String name;
    private String  ROOT_DIR=System.getProperty("java.io.tmpdir")+File.separator+"logs";
    private File logFile;
    public JavaProcessLauncher(Class mainClass, String[] args,String name){
        this.mainClass= mainClass;
        this.args= args;
        this.name=name;
    }

    /**
     * Start process
     *
     **/
    public void start() throws Exception{
        String classpath = System.getProperty("java.class.path");
        logger.info(String.format("Using class path:%s",classpath));
        logger.info(String.format("Launch main class:%s",mainClass.getName()));
        String[] defaultCommands=new String[]{"java","-cp",classpath,mainClass.getName()};
        String[] commands=new String[defaultCommands.length+args.length];
        for(int i=0;i<defaultCommands.length;i++){
            commands[i]= defaultCommands[i];
        }
        System.arraycopy(args,0,commands,defaultCommands.length,args.length);
        ProcessBuilder builder=new ProcessBuilder(commands);
//      builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        redirectOutputToLog(this.name,builder);
        builder.redirectErrorStream(true);
        process=builder.start();
        process.getOutputStream().close();
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            int pid = (int) pidField.get(process);
            logger.info("starting process {},{}",this.name,pid);
        } else {
            logger.info("starting process {}",this.name);
        }


    }

    /**
     * For local debug use
     * @param sign  a unique sign for the process
     *
     **/
    public void redirectOutputToLog(String sign,ProcessBuilder builder) throws Exception{
        File file=new File(ROOT_DIR);
        if(!file.exists()){
            file.mkdirs();
        }
        logFile =new File(ROOT_DIR+File.separator+sign+".log");
        logFile.createNewFile();
        builder.redirectOutput(logFile);

    }

    /**
     * Wait for process start ready
     *
     **/
    public boolean waitForReady(int timeout, TimeUnit unit) throws Exception{
        return process.waitFor(timeout,unit);
    }

    /**
     *
     * Stop process
     *
     **/
    public void destroy(){
        logger.info("destroy {}",this.name);
        process.destroy();
//        if(logFile!=null&&logFile.exists()){
//            logFile.delete();
//        }

    }
}
