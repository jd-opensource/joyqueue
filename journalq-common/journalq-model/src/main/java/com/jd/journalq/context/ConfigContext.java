package com.jd.journalq.context;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.event.ConfigEvent;
import com.jd.journalq.exception.JMQConfigException;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * broker 上下文.
 *
 * @author lindeqiang
 * @since 2016/8/19 14:03
 */
public abstract class ConfigContext extends ConfigDef implements EventListener<ConfigEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigContext.class);
    public static final String GROUP_STORE = "store";
    public static final String GROUP_MESSAGE = "message";
    public static final String GROUP_BROKER = "broker";
    public static final String GROUP_ELECTION = "election";
    public static final String GROUP_RETRY = "retry";
    public static final String GROUP_CONSUME = "consume";
    public static final String GROUP_PRODUCE = "produce";
    public static final String GROUP_SERVER = "server";
    public static final String GROUP_NAMESERVER = "nameserver";
    public static final String GROUP_ARCHIVE = "archive";
    public static final String GROUP_BROKER_MANAGER = "manager";
    public static final String GROUP_BROKER_MONITOR = "monitor";

    public static final String GROUP_KAFKA = "kafka";
    public static final String GROUP_JMQ = "jmq";
    private String rootDir = null;
    private Broker broker;
    private String brokerIp;
    private Integer brokerPort;


    public ConfigContext() {
    }

    /**
     * 加载配置文件
     *
     * @return
     */
    public ConfigContext load() {
        Properties properties = new Properties();
        try {
            for (String config : getConfigPath()) {
                Properties propertiesbak = new Properties();
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(config);
                if (null == inputStream) {
                    if(must()){
                        throw new Exception(String.format("config [%s] is not exist", config));
                    }else{
                        logger.warn("config [%s] is not exist", config);
                    }
                }
                propertiesbak.load(inputStream);
                for(String key : propertiesbak.stringPropertyNames()){
                    if(key.startsWith(getConfigGroup()+".")){
                        if(key.startsWith(getConfigGroup())){
                            properties.put(key.substring(getConfigGroup().length()+1),propertiesbak.getProperty(key));
                        }
                    }else{
                        properties.put(key,propertiesbak.getProperty(key));
                    }
                }
            }
            doParse(properties);
            return this;
        } catch (Exception e) {
            String mesg = String.format("init config %s error", this.getClass().getSimpleName());
            logger.error(mesg, e);
            throw new JMQConfigException(mesg, e);
        }
    }


    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public void setBrokerPort(Integer brokerPort) {
        this.brokerPort = brokerPort;
    }

    public Broker getBroker() {
        return broker;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public Integer getBrokerPort() {
        return brokerPort;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    /**
     * 配置文件路径
     *
     * @return
     */
    public String[] getConfigPath(){
        return new String[]{"laf-jmq.properties"};
    };
    /**
     * 配置文件是否必须
     *
     * @return
     */
    public boolean must(){return false;};

    /**
     * 配置文件分组
     *
     * @return
     */
    public abstract String getConfigGroup();

    /**
     * @param configs
     */
    protected abstract void doParse(Map<Object, Object> configs);
}
