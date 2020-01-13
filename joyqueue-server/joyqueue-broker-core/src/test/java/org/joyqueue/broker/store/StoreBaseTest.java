package org.joyqueue.broker.store;

import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.broker.config.Configuration;
import org.joyqueue.broker.config.ConfigurationManager;
/**
 * Base test for store test
 *
 **/
public class StoreBaseTest {


    public Configuration config() throws Exception{
        ConfigurationManager configurationManager = new ConfigurationManager(new String[0]);
                             configurationManager.start();
        Configuration config= configurationManager.getConfiguration();
        //build broker config
        BrokerConfig brokerConfig = new BrokerConfig(config);
        brokerConfig.getAndCreateDataPath();
        return config;
    }
}
