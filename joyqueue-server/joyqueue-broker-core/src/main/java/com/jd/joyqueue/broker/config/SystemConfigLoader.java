package com.jd.joyqueue.broker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * SystemConfigLoader
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/26
 */
public class SystemConfigLoader {

    protected static final Logger logger = LoggerFactory.getLogger(SystemConfigLoader.class);

    protected static final String SYSTEM_EVN_FILE = "system.properties";

    public static void load() {
        try {
            Properties sysEvn = new Properties();
            InputStream inputStream = SystemConfigLoader.class.getClassLoader().getResourceAsStream(SYSTEM_EVN_FILE);
            if (null != inputStream) {
                sysEvn.load(inputStream);
            }
            sysEvn.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
        } catch (IOException e) {
            logger.warn("load system config exception", e);
        }
    }
}