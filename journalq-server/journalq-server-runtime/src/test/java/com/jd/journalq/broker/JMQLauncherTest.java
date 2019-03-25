package com.jd.journalq.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chengzhiliang on 2018/9/27.
 */
public class JMQLauncherTest {

    protected static final Logger logger = LoggerFactory.getLogger(JMQLauncher.class);

    public static void main(String[] args) throws Exception {
        BrokerServiceTest brokerService = new BrokerServiceTest();
        try {
            brokerService.start();
            logger.info("JMQLauncher is start");
        } catch (Throwable t) {
            logger.error("JMQLauncher start exception", t);
            brokerService.stop();
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    brokerService.stop();
                } catch (Throwable t) {
                    logger.error("JMQLauncher stop exception", t);
                    System.exit(-1);
                }
            }
        });
    }
}