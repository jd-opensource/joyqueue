package com.jd.journalq.server.retry.remote.config;

import com.jd.journalq.exception.JMQConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by chengzhiliang on 2019/1/26.
 */
public class RemoteRetryConfig {
    private final static Logger logger = LoggerFactory.getLogger(RemoteRetryConfig.class);

    // 远程重试限制的线程数
    private static int remoteRetryLimitThread;
    // 更新远程重试实例的地址时间讲个
    private static int remoteRetryUpdateInterval;

    // 配置文件路径
    private static final String configFile = "laf-jmq.properties";


    // 初始化数据源配置
    static {
        Properties properties = new Properties();
        InputStream inputStream = RemoteRetryConfig.class.getClassLoader().getResourceAsStream(configFile);
        if (null == inputStream) {
            throw new JMQConfigException("cannot load laf-jmq.properties.");
        }
        try {
            properties.load(inputStream);

            remoteRetryLimitThread = Integer.parseInt(properties.getProperty(Enum.REMOTE_RETRY_LIMIT_THREADS.name, Enum.REMOTE_RETRY_LIMIT_THREADS.value));
            remoteRetryUpdateInterval = Integer.parseInt(properties.getProperty(Enum.REMOTE_RETRY_UPDATE_INTERVAL.name, Enum.REMOTE_RETRY_UPDATE_INTERVAL.value));
        } catch (IOException e) {
            throw new JMQConfigException("load and parse config error.", e);
        }

        logger.info("success init RemoteRetryConfig.");
    }

    enum Enum {
        REMOTE_RETRY_LIMIT_THREADS("retry.remote.retry.limit.thread", "10"),
        REMOTE_RETRY_UPDATE_INTERVAL("retry.remote.retry.update.interval", "60000");

        String name;
        String value;

        Enum(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static int getRemoteRetryLimitThread() {
        return remoteRetryLimitThread;
    }

    public static int getRemoteRetryUpdateInterval() {
        return remoteRetryUpdateInterval;
    }
}
