package io.chubao.joyqueue.broker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * SystemConfigLoader
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/26
 */
public class SystemConfigLoader {

    protected static final Logger logger = LoggerFactory.getLogger(SystemConfigLoader.class);
    protected static final Properties DEFAULT_PROPERTIES = new Properties();

    protected static final String SYSTEM_EVN_FILE = "system.properties";

    static {
        DEFAULT_PROPERTIES.setProperty("IGNITE_QUIET", "true");
        DEFAULT_PROPERTIES.setProperty("IGNITE_DUMP_THREADS_ON_FAILURE", "false");
        DEFAULT_PROPERTIES.setProperty("IGNITE_NO_ASCII", "true");
        DEFAULT_PROPERTIES.setProperty("IGNITE_UPDATE_NOTIFIER", "false");
    }

    public static void load() {
        Properties sysEvn = new Properties();

        try {
            URL sysEnvFileUrl = SystemConfigLoader.class.getClassLoader().getResource(SYSTEM_EVN_FILE);
            if(null != sysEnvFileUrl) {
                logger.info("Found system properties file: {}.", sysEnvFileUrl);
                sysEvn.load(sysEnvFileUrl.openStream());
            } else {
                logger.info("No system properties file in classpath, using default properties.");
                sysEvn = DEFAULT_PROPERTIES;
            }
        } catch (IOException e) {
            logger.warn("load system config exception, using default.", e);
            sysEvn = DEFAULT_PROPERTIES;
        }

        sysEvn.forEach((k, v) -> {
            logger.info("Set system property: {} -> {}.", k, v);
            System.setProperty(k.toString(), v.toString());
        });

    }
}