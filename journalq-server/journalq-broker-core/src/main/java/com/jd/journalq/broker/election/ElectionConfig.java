package com.jd.journalq.broker.election;

import com.jd.journalq.toolkit.config.Property;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/13
 */
public class ElectionConfig {
    public static final String ELECTION_META_PATH ="/election";
    private String electionMetaPath ;
    private String electionMetaFile;
    private PropertySupplier propertySupplier;

    private int listenPort;

    private String electionMetaPathStub;

    public ElectionConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getMetadataPath() {
        if (electionMetaPath == null || electionMetaPath.isEmpty()) {
            synchronized (this) {
                if (electionMetaPath == null) {
                    String prefix = "";
                    if (propertySupplier != null) {
                        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
                        prefix = property == null ? prefix : property.getString();
                    }
                    electionMetaPath = prefix + ELECTION_META_PATH;
                }

            }
        }
        return electionMetaPath;
    }

    public String getMetadataFile() {
        if (electionMetaPathStub != null) {
            return electionMetaPathStub;
        }
		
        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
        String prefix = property == null ? "" : property.getString();

        return prefix + PropertySupplier.getValue(propertySupplier, ElectionConfigKey.ELECTION_METADATA);
    }

    public int getElectionTimeout() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.ELECTION_TIMEOUT);
    }

    public int getExecutorThreadNumMin() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.EXECUTOR_THREAD_NUM_MIN);
    }

    public int getExecutorThreadNumMax() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.EXECUTOR_THREAD_NUM_MAX);
    }

    public int getTimerScheduleThreadNum() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.TIMER_SCHEDULE_THREAD_NUM);
    }

    public int getHeartbeatTimeout() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.HEARTBEAT_TIMEOUT);
    }

    public int getSendCommandTimeout() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.SEND_COMMAND_TIMEOUT);
    }

    public int getMaxReplicateLength() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.MAX_BATCH_REPLICATE_SIZE);
    }

    public int getDisableStoreTimeout() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.DISABLE_STORE_TIMEOUT);
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getTransferLeaderTimeout() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.TRANSFER_LEADER_TIMEOUT);
    }

    public int getReplicateConsumePosInterval() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.REPLICATE_CONSUME_POS_INTERVAL);
    }

    public int getReplicateThreadNumMin() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.REPLICATE_THREAD_NUM_MIN);
    }

    public int getReplicateThreadNumMax() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.REPLICATE_THREAD_NUM_MAX);
    }

    public int getCommandQueueSize() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.COMMAND_QUEUE_SIZE);
    }

    public int getLogInterval() {
        return PropertySupplier.getValue(propertySupplier, ElectionConfigKey.LOG_INTERVAL);
    }


    public void setListenPort(String port) {
        listenPort = Integer.valueOf(port);
    }

    public void setElectionMetaPath(String electionMetaPath) {
        this.electionMetaPathStub = electionMetaPath;
    }
}
