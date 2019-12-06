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

import io.chubao.joyqueue.toolkit.time.SystemClock;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Launcher a new process
 *
 **/
public class JavaProcessLauncher {
    private Class mainClass;
    private String[] args;
    private String logFile;
    private File log;
    private String startSignLine;
    private Process process;
    public JavaProcessLauncher(Class mainClass, String[] args, String logFile, String startSignLine){
        this.mainClass= mainClass;
        this.args= args;
        this.logFile= logFile;
        this.startSignLine=startSignLine;
    }

    /**
     * Start process
     *
     **/
    public void start() throws Exception{
        String classpath = System.getProperty("java.class.path");
        System.out.println(String.format("Using class path:%s",classpath));
        System.out.println(String.format("Launch main class:%s",mainClass.getName()));
        String[] defaultCommands=new String[]{"java","-cp",classpath,mainClass.getName()};
        String[] commands=new String[defaultCommands.length+args.length];
        for(int i=0;i<defaultCommands.length;i++){
            commands[i]= defaultCommands[i];
        }
        System.arraycopy(args,0,commands,defaultCommands.length,args.length);
        ProcessBuilder builder=new ProcessBuilder(commands);
         this.log=new File(logFile);
        if(!log.getParentFile().exists()){
            log.getParentFile().mkdirs();
        }
        if(!log.exists()){
            log.createNewFile();
        }
        builder.redirectError(log);
        builder.redirectOutput(log);
        builder.redirectInput(log);
        process=builder.start();
        System.out.println("starting process success");

    }
    /**
     * Wait for process start ready
     *
     **/
    public void waitForReady(int timeout, TimeUnit unit) throws Exception{
             if(startSignLine==null){
                 process.waitFor(timeout,unit);
             }else{
                 long startTimeMs= SystemClock.now();
                 long timeOutMs=startTimeMs + unit.toMillis(timeout);
                 tailAndFindSignLine(process.getInputStream(),process.getOutputStream(),timeOutMs);
             }
    }

    /**
     * Tail process output and read until sign line or timeout
     *
     **/
    public void tailAndFindSignLine(InputStream inputStream, OutputStream outputStream, long timeoutMs) throws Exception{
//        Scanner reader = new Scanner(inputStream);
//        DataOutputStream logs=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.log)));
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.log));
        boolean success=false;
        while(timeoutMs>SystemClock.now()) {
            String cur;
            if((cur = bufferedReader.readLine())!=null) {
                System.out.println(cur);
                if (cur.contains(startSignLine)) {
                    success = true;
                    break;
                }
            }
        }
        bufferedReader.close();
        if(!success){
            throw new TimeoutException("Read sign line timeout !");
        }else{
            System.out.println(String.format("Find sign line: %s",startSignLine));
        }
    }

    /**
     *
     * Stop process
     *
     **/
    public void destroy(){
        process.destroy();
    }
}
