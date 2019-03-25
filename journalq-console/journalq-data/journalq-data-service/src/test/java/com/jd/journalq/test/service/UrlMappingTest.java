package com.jd.journalq.test.service;

import com.jd.journalq.service.BrokerRestUrlMappingService;
import com.jd.journalq.service.impl.BrokerRestUrlMappingServiceImpl;
import org.junit.Test;

public class UrlMappingTest {


    @Test
    public void urlTest(){
        BrokerRestUrlMappingService urlMappingService=new BrokerRestUrlMappingServiceImpl();
        String url=urlMappingService.urlTemplate("appClientMonitor");
        System.out.println(url);

    }
}
