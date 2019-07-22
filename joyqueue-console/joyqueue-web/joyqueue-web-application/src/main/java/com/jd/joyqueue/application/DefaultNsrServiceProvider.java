package com.jd.joyqueue.application;

import com.jd.joyqueue.nsr.NsrServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.InvalidPropertiesFormatException;

/**
 * @author liyue25
 * Date: 2019-07-22
 */
@Service
public class DefaultNsrServiceProvider implements NsrServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultNsrServiceProvider.class);
    private int nextBrokerIndex = 0;
    @Value("${joyqueue.servers}")
    private String [] servers;
    private static final String PATTERN = "^(([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)*([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])(:[0-9]+)?$";
    @PostConstruct
    public void parseBrokers( ) throws InvalidPropertiesFormatException {
        if(servers == null || servers.length == 0) {
            servers = new String[] {"127.0.0.1"};
        }

        for (int i = 0; i < servers.length; i++) {
            servers[i] = servers[i].trim();
            if (!servers[i].matches(PATTERN)) {
                throw new InvalidPropertiesFormatException("Invalid property! joyqueue.servers: " + servers);
            }
            if(!servers[i].contains(":")) {
                servers[i] += ":50092";
            }
        }
        logger.info("Using nameservers: {}.", String.join(",", servers));
    }


    @Override
    public String getBaseUrl() {
        nextBrokerIndex = (nextBrokerIndex + 1) % servers.length;
        return "http://" + servers[nextBrokerIndex];
    }
}
