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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.joyqueue.nsr.Admin;
import org.joyqueue.nsr.CommandArgs;
import java.util.Map;
/**
 *  Only support sub command
 *
 **/
public abstract class AbstractAdmin extends CommandArgs implements Admin {

    @Override
    public void execute(JCommander jc, String[] args, Map<String, CommandArgs> commandArgsMap) {
        try {
            jc.parse(args);
            if (help) {
                jc.usage();
                System.exit(-1);
            }
            boolean subCommandHelp=false;
            for(CommandArgs arg:commandArgsMap.values()){
                if(arg.help) {subCommandHelp=true;}
            }
            if(subCommandHelp){
                jc.getCommands().get(jc.getParsedCommand()).usage();
                System.exit(-1);
            }
            String command=jc.getParsedCommand();
            process(command,commandArgsMap.get(command), jc);
            close();
        } catch (ParameterException e) {
            System.out.println("bad args:"+e.getMessage());
            jc.usage();
            System.exit(-1);
        }catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }

    }

    /**
     * Command process
     * @param command  command name
     * @param arguments  command args
     * @param jCommander command
     *
     **/
    public abstract void process(String command,CommandArgs arguments,JCommander jCommander) throws Exception;
}
