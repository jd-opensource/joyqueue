package com.jd.journalq.nsr.admin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.jd.journalq.nsr.Admin;
import com.jd.journalq.nsr.CommandArgs;
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
