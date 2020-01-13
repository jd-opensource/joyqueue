package org.joyqueue.broker.store;

import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Ensure not out of global storage limit. first, clean out of date message log and try to clean
 * oldest message log of partition group if storage size is still greater than storage limit
 *
 **/
public class GlobalStorageLimitCleaningStrategy implements StoreCleaningStrategy {
    private static final Logger LOG= LoggerFactory.getLogger(GlobalStorageLimitCleaningStrategy.class);
    private long forceCleanWALStorageSizeThreshold;
    private long stopCleanWALStorageSizeThreshold;
    private int  forceCleanBufferFraction;
    private String applicationDataPath;
    private File applicationDataDirectory;
    private boolean keepUnconsumed;
    private long maxStoreTime;
    private long totalStorageSize;
    private Map<String,Boolean> partitionGroupForcibleCleanWal=new HashMap();
    private StorageState storageState=StorageState.SAFETY;
    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, Map<Short, Long> partitionAckMap) throws IOException {
        if(partitionGroupStore==null){
            return -1;
        }
        long now = SystemClock.now();
        long used=0L;
        long cleanWALBeforeTime = now - maxStoreTime;
        long totalDeletedSize = 0L;  // 总共删除长度
        long deletedSize = 0L;
        do {
            deletedSize = partitionGroupStore.clean(cleanWALBeforeTime, partitionAckMap, keepUnconsumed);
            totalDeletedSize += deletedSize;
        } while (deletedSize > 0L);
        String partitionGroupKey= String.format("%s:%d",partitionGroupStore.getTopic(),partitionGroupStore.getPartitionGroup());
        // first, clean out of date message log for all partition groups and we may force clean consumed log
        Boolean force=partitionGroupForcibleCleanWal.get(partitionGroupKey);
        if(force==null){ partitionGroupForcibleCleanWal.put(partitionGroupKey,true);
                         force=false;}

        if(force&&storageState==StorageState.CLEANING){
            if((used=usedStorageSize())>stopCleanWALStorageSizeThreshold) {
                totalDeletedSize += partitionGroupStore.clean(-1, partitionAckMap, keepUnconsumed);
            }else{
                storageState=StorageState.SAFETY;
                partitionGroupForcibleCleanWal.clear();
                LOG.info("current storage state:{},force clean storage threshold {} byte,used storage {} byte",storageState,forceCleanWALStorageSizeThreshold,used);
            }
        }else if(force&&StorageState.SAFETY==storageState&&(used=usedStorageSize())>=forceCleanWALStorageSizeThreshold){
                storageState= StorageState.CLEANING;
                LOG.info("current storage state:{},force clean storage threshold {} byte,used storage {} byte",storageState,forceCleanWALStorageSizeThreshold,used);
        }
        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        BrokerStoreConfig brokerStoreConfig = new BrokerStoreConfig(supplier);
        int forceCleanWALFractionThreshold= brokerStoreConfig.getForceCleanWALFractionThreshold();
        this.applicationDataPath= brokerStoreConfig.getApplicationDataPath();
        this.forceCleanBufferFraction= brokerStoreConfig.getForceCleanWALBufferFraction();
        this.applicationDataDirectory=new File(this.applicationDataPath);
        this.totalStorageSize=totalStorageSize();
        this.keepUnconsumed = brokerStoreConfig.keepUnconsumed();
        this.maxStoreTime = brokerStoreConfig.getMaxStoreTime();
        this.forceCleanWALStorageSizeThreshold= totalStorageSize*(forceCleanWALFractionThreshold)/100;
        this.stopCleanWALStorageSizeThreshold= totalStorageSize*(forceCleanWALFractionThreshold-forceCleanBufferFraction)/100;
    }

    /**
     * SAFETY   don't need any clean
     * CLEANING cleaning
     **/
    public enum StorageState{
        SAFETY,CLEANING
    }

    /**
     * return used storage size or -1
     **/
    public long usedStorageSize(){
        long usableStorageSize=0;
        if(applicationDataDirectory.exists()) {
            usableStorageSize= applicationDataDirectory.getUsableSpace();
        }
        return totalStorageSize-usableStorageSize;
    }

    public long totalStorageSize(){
        if(applicationDataDirectory.exists()) {
           return applicationDataDirectory.getTotalSpace();
        }
        return -1;
    }
}
