package com.jd.journalq.nsr;

import com.beust.jcommander.JCommander;

import java.io.Closeable;
import java.util.Map;

public interface Admin extends Closeable {

    /**
     * @param args  input command args
     * @param commandArgsMap  sub command args
     *
     **/
     void execute(JCommander jCommander, String[] args,Map<String,CommandArgs> commandArgsMap);

}
