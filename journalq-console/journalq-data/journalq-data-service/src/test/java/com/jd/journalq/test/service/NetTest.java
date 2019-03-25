package com.jd.journalq.test.service;

import com.jd.journalq.model.domain.BrokerMonitorRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class NetTest {
    Logger logger= LoggerFactory.getLogger(NetTest.class);

    @Test
    public void test(){
        logger.info("aa");

    }

    @Test
    public void lamdaSort(){

        Random random=new Random();
        List<BrokerMonitorRecord> recordList=new ArrayList();
        BrokerMonitorRecord record;
        for(int i=0;i<100;i++){
            record=new BrokerMonitorRecord();
            record.setPartition(random.nextInt(100));
            recordList.add(record);
        }

        recordList.sort(Comparator.comparing(e->e.getPartition()));
        System.out.println();

    }
}
