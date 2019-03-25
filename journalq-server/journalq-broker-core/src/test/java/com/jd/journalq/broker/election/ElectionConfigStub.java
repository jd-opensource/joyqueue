package com.jd.journalq.broker.election;

import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * Created by zhuduohui on 2018/10/17.
 */
public class ElectionConfigStub extends ElectionConfig {
    //TODO
    /*ConfigDef configDefEx = new ConfigDef()
            .define(metadataFile, "raft_metafile.dat", ConfigDef.Type.STRING)
            .define(electionTimeout, 1000 * 3, ConfigDef.Type.INT)
            .define(executorThreadNumMin, 10, ConfigDef.Type.INT)
            .define(executorThreadNumMax, 100, ConfigDef.Type.INT)
            .define(timerScheduleThreadNum, 10, ConfigDef.Type.INT)
            .define(heartbeatTimeout, 1000 * 1, ConfigDef.Type.INT)
            .define(sendCommandTimeout, 1000 * 10, ConfigDef.Type.INT)
            .define(maxBatchReplicateMessages, 10, ConfigDef.Type.INT)
            .define(disableStoreTimeout, 1000 * 5, ConfigDef.Type.INT)
            .define(listenPort, 18001, ConfigDef.Type.INT)
            .define(transferLeaderTimeout, 1000 * 10, ConfigDef.Type.INT)
            .define(replicateConsumePosInterval, 1000 * 5, ConfigDef.Type.INT)
            .define(maxReplicateInterval, 1000 * 60, ConfigDef.Type.INT)
            .define(replicateThreadNumMin, 10, ConfigDef.Type.INT)
            .define(replicateThreadNumMax, 100, ConfigDef.Type.INT);*/

    public ElectionConfigStub(PropertySupplier propertySupplier) {
        super(propertySupplier);
    }

    @Deprecated
    public void setListenPort(String port) {
        // do nothing
    }

    @Deprecated
    public void setMetadataFile(String metadataFile) {
        // do nothing
    }
}
