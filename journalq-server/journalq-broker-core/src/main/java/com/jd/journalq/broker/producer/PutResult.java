package com.jd.journalq.broker.producer;

import com.jd.journalq.store.WriteResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lining11
 * Date: 2018/8/30
 */
public class PutResult {

    private Map<Short,WriteResult> writeResults = new HashMap<>();

    public void addWriteResult(Short partition ,WriteResult writeResult){
        writeResults.put(partition,writeResult);
    }

    public Map<Short,WriteResult> getWriteResults(){
        return writeResults;
    }

}
