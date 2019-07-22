package com.jd.joyqueue.application;

import com.jd.joyqueue.domain.Broker;
import com.jd.joyqueue.nsr.NsrServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author liyue25
 * Date: 2019-07-22
 */
@Service
public class DefaultNsrServiceProvider implements NsrServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultNsrServiceProvider.class);
    private List<Broker> brokers = null;
    private int nextBrokerIndex = 0;
    @Value("${joyqueue.servers}")
    private String [] servers;

    @PostConstruct
    public void parseBrokers( ) throws InvalidPropertiesFormatException {
        if(servers == null || servers.length == 0) {
            servers = new String[] {"127.0.0.1:50088"};
        }

        brokers = Arrays.stream(servers).map(server -> {
            try {
                Broker broker = new Broker();
                String[] splt = server.split(":");
                String host = splt[0].trim();
                int port = Integer.valueOf(splt[1].trim());
                broker.setIp(host);
                broker.setPort(port);
                return broker;
            } catch (Throwable t) {
                logger.warn("Invalid host port: {}, {}", server, t);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if(brokers.isEmpty()) {
            throw new InvalidPropertiesFormatException("Invalid property! joyqueue.servers: " + servers);
        }
    }


    @Override
    public String getBaseUrl() {
        Broker broker = brokers.get(nextBrokerIndex);
        nextBrokerIndex = (nextBrokerIndex + 1) % brokers.size();
        return "http://" + broker.getIp() + ":" + broker.getManagerPort();
    }
}
