package io.chubao.joyqueue.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher
 *
 * author: gaohaoxiang
 * date: 2018/8/27
 */
public class Launcher {

    protected static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        BrokerService brokerService = new BrokerService(args);

        try {
            brokerService.start();
            BannerPrinter.print();
            logger.info("JoyQueue is started");
        } catch (Throwable t) {
            logger.error("JoyQueue start exception", t);
            brokerService.stop();
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                brokerService.stop();
                logger.info("JoyQueue stopped");
            } catch (Throwable t) {
                logger.error("JoyQueue stop exception", t);
                System.exit(-1);
            }
        }));
    }
}