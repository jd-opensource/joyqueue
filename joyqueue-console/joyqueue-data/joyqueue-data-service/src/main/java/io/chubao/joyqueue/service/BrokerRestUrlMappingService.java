package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Broker;

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
