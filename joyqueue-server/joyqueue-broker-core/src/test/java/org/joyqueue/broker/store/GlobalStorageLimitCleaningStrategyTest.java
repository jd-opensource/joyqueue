package org.joyqueue.broker.store;

import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.broker.config.Configuration;
import org.joyqueue.store.*;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.store.utils.MessageUtils;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalStorageLimitCleaningStrategyTest extends StoreBaseTest {

    private StoreService storeService;
    private Configuration config;
    private StoreConfig storeConfig;
    private long totalStorageSize;
    private long forceCleanStorageSize;
    private long forceCleanStopStorageSize;
    private String topic="testTopic";
    private int partitionGroup=1;
    private short partitionForPartitionGroup=5;
    private long writeMsgMs;
    private GlobalStorageLimitCleaningStrategy cleaningStrategy=new GlobalStorageLimitCleaningStrategy();
    @Before
    public void prepare() throws Exception{
            this.config = config();
            this.storeConfig=new StoreConfig(config);
            this.storeService= new Store(storeConfig);
            ((Store) this.storeService).start();
            createTopic();
            cleaningStrategy.setSupplier(config);
            int forceCleanFraction= computeForceCleanFraction();
            int safeFraction=forceCleanFraction-1;
            config.addProperty(BrokerStoreConfig.BrokerStoreConfigKey.STORE_DISK_USAGE_MAX.getName(),String.valueOf(forceCleanFraction));
            config.addProperty(BrokerStoreConfig.BrokerStoreConfigKey.STORE_DISK_USAGE_SAFE.getName(),String.valueOf(safeFraction));
            // update
            cleaningStrategy.setSupplier(config);
            totalStorageSize=cleaningStrategy.totalStorageSize();
            forceCleanStorageSize=totalStorageSize*forceCleanFraction/100;
            forceCleanStopStorageSize=totalStorageSize*safeFraction/100;
            // write message
            writeMsgMs=writeMessagesUntil();
            long oneMinutes=60*1000;
            // make sure no message expired
            updateMessageExpireTime(writeMsgMs+oneMinutes);
    }
    public void createTopic() throws Exception {
        for(int i=0;i<partitionGroup;i++){
            this.storeService.createPartitionGroup(topic,i,partitions((short)(i*partitionForPartitionGroup),partitionForPartitionGroup));
            this.storeService.getReplicableStore(topic, i).enable();
        }
    }

    public short[] partitions(short partitionOffset,short count){
        short[] partitions=new short[count];
        for(short i=0;i<count;i++){
            partitions[i]= (short) (partitionOffset+i);
        }
        return partitions;
    }

    /**
     * 根据应用所在磁盘的存储情况，计算force clean fraction,大于used fraction且至少2个fraction的可写入存储空间
     **/
    public int computeForceCleanFraction() {
        long totalStorageSize=cleaningStrategy.totalStorageSize();
        long used=cleaningStrategy.usedStorageSize();
        int usedFraction=(int)(used*100/totalStorageSize);
        long forceMaxFraction=90;
        int forceFraction=usedFraction;
        // 保证至少可以写入5个消息文件
        long miniMessageStorage = storeConfig.getMessageFileSize()*5;

        if(usedFraction>forceMaxFraction){
            throw new IllegalStateException("no enough storage ");
        }
        do {
            forceFraction+=1;
        }while (forceFraction<forceMaxFraction&&((forceFraction-usedFraction)*totalStorageSize/100 < miniMessageStorage||forceFraction-usedFraction<2));
        if(forceFraction>=forceMaxFraction){
            throw new IllegalStateException("no enough storage ");
        }
        return forceFraction;
    }

    /**
     *
     * Mock once force clean wal
     *
     **/
    @Test
    public void cleanExpiredWALTest() throws IOException{
        // message expired
        updateMessageExpireTime(writeMsgMs/2);
        // assume cleaning state
        long startMs=SystemClock.now();
        // 5 minutes clean timeout
        long timeout=3*60*1000;
        long used=0;
        long totalCleanedSize=0;
        do {
            for (int i = 0; i < partitionGroup; i++) {
                PartitionGroupStore pgstore = storeService.getStore(topic, i);
                Map<Short, Long> consumeAck = new HashMap();
                for (short p : pgstore.listPartitions()) {
                    consumeAck.put(p, storeService.getManageService().partitionMetric(topic,p).getRightIndex());
                }
                totalCleanedSize+=cleaningStrategy.deleteIfNeeded(pgstore, consumeAck, null);

            }
            // 检查是否出现过 cleaning 状态
            used=cleaningStrategy.usedStorageSize();
        } while (used>forceCleanStopStorageSize&&SystemClock.now()-startMs<timeout);
        Assert.assertTrue(totalCleanedSize>0);
    }

    @Test
    public void forceCleanWALOnce() throws IOException{
        long totalCleanedSize=0;
        int hasCleaningState=0;
        long plusTime=60*1000;
        // no expire message
        updateMessageExpireTime(writeMsgMs+plusTime);
        for(int k=0;k<3;k++) {
            for (int i = 0; i < partitionGroup; i++) {
                PartitionGroupStore pgstore = storeService.getStore(topic, i);
                Map<Short, Long> consumeAck = new HashMap();
                for (short p : pgstore.listPartitions()) {
                    consumeAck.put(p, storeService.getManageService().partitionMetric(topic, p).getRightIndex());
                }
                hasCleaningState=hasCleaningState|cleaningStrategy.state().ordinal();
                totalCleanedSize+=cleaningStrategy.deleteIfNeeded(pgstore, consumeAck, null);
            }
        }
        Assert.assertTrue(hasCleaningState>0);
        Assert.assertTrue(totalCleanedSize>0);

    }


    @After
    public void close(){
        for (int i = 0; i < partitionGroup; i++) {
            storeService.removePartitionGroup(topic, i);
        }
        ((Store)storeService).stop();
        ((Store)storeService).physicalDelete();
    }

    /**
     * 设置消息存储的最大时长
     *
     **/
    public void updateMessageExpireTime(long keeptime){
        config.addProperty(BrokerStoreConfig.BrokerStoreConfigKey.MAX_STORE_TIME.getName(),String.valueOf(keeptime));
        cleaningStrategy.setSupplier(config);
    }

    /**
     * @return  写入耗时
     *
     **/
    public long writeMessagesUntil(){
        List<ByteBuffer> messages = MessageUtils.build(1024, 1024);
        long startMs= SystemClock.now();
        do{
            for(int i=0;i<partitionGroup;i++) {
                PartitionGroupStore pgstore = storeService.getStore(topic,i );
                for(short p:pgstore.listPartitions()){
                    WriteRequest[] writeRequests = messages.stream().map(b -> {
                        MessageParser.setLong(b,MessageParser.CLIENT_TIMESTAMP,SystemClock.now());
                        return new WriteRequest(p, b);
                    }).toArray(WriteRequest[]::new);
                    pgstore.asyncWrite(writeRequests);
                }
            }
        }while(cleaningStrategy.usedStorageSize()<forceCleanStorageSize);
        return SystemClock.now()-startMs;
    }



}
