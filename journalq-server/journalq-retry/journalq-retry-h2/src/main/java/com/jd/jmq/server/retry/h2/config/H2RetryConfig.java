package com.jd.journalq.server.retry.h2.config;

import com.jd.journalq.exception.JMQConfigException;
import com.jd.journalq.datasource.DataSourceConfig;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by chengzhiliang on 2019/3/11.
 */
public class H2RetryConfig {

    enum Enum {
        WRITE_URL("retry.h2.url.write", ""),
        WRITE_USER_NAME("retry.h2.username.write", ""),
        WRITE_PASSWORD("retry.h2.password.write", ""),
        DRIVER("retry.h2.driver", ""),
        RETRY_DELAY("retry.delay", "1000"),
        MAX_RETRY_TIMES("retry.max.retry.times", "3");

        String name;
        String value;

        Enum(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(H2RetryConfig.class);


    // 写库URL
    private static String writeUrl;
    // 写库用户名
    private static String writeUserName;
    // 写库密码
    private static String writePassword;
    // mysql驱动
    private static String driver;
    // 重试间隔
    private static int retryDelay = 1000;
    // 最大重试次数
    private static int maxRetryTimes = 3;
    // 重试策略
    private static RetryPolicy retryPolicy;
    // 写据源配置
    private static DataSourceConfig dsConfig;
    // 配置路径加名称
    private static final String configFile = "laf-jmq.properties";

    // 初始化数据源配置
    static {
        Properties properties = new Properties();
        InputStream inputStream = H2RetryConfig.class.getClassLoader().getResourceAsStream(configFile);
        if (null == inputStream) {
            throw new JMQConfigException("cannot load laf.properties.");
        }
        try {
            properties.load(inputStream);

            writeUrl = properties.getProperty(Enum.WRITE_URL.name, Enum.WRITE_URL.value);
            writeUserName = properties.getProperty(Enum.WRITE_USER_NAME.name, Enum.WRITE_USER_NAME.value);
            writePassword = properties.getProperty(Enum.WRITE_PASSWORD.name, Enum.WRITE_PASSWORD.value);
            driver = properties.getProperty(Enum.DRIVER.name, Enum.DRIVER.value);
            retryDelay = Integer.parseInt(properties.getProperty(Enum.RETRY_DELAY.name, Enum.RETRY_DELAY.value));
            maxRetryTimes = Integer.parseInt(properties.getProperty(Enum.MAX_RETRY_TIMES.name, Enum.MAX_RETRY_TIMES.value));

            DataSourceConfig writeDataSource = new DataSourceConfig();
            writeDataSource.setDriver(getDriver());
            writeDataSource.setUrl(getWriteUrl());
            writeDataSource.setUser(getWriteUserName());
            writeDataSource.setPassword(getWritePassword());

            retryPolicy = new RetryPolicy(getRetryDelay(), getMaxRetryTimes());

            dsConfig = writeDataSource;
        } catch (IOException e) {
            throw new JMQConfigException("load and parse config error.", e);
        }

        logger.info("Success init DbRetryConfig.");
    }

    public static String getWriteUrl() {
        return writeUrl;
    }

    public static String getWriteUserName() {
        return writeUserName;
    }

    public static String getWritePassword() {
        return writePassword;
    }

    public static String getDriver() {
        return driver;
    }

    public static RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public static DataSourceConfig getDsConfig() {
        return dsConfig;
    }

    public static int getRetryDelay() {
        return retryDelay;
    }

    public static int getMaxRetryTimes() {
        return maxRetryTimes;
    }
}
