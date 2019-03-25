package com.jd.journalq.service;

import com.jd.journalq.model.domain.Broker;

//todo 用BrokerUrlTemplateMappingUtil替代
public interface BrokerRestUrlMappingService {

    /**
     *
     * @return  key 对应的path
     *
     **/
    String pathTemplate(String key);

    /**
     *
     * @param key  path key
     * @return  key 对应的path
     * 带有ip:port template前缀
     *
     **/
    String urlTemplate(String key);

    /**
     *
     * @return  http://ip:port
     *
     **/
    String monitorUrl(Broker broker);


    /**
     *
     * @return  http://ip:port
     *
     **/
    String url(String ip,int port);
}
