package com.jd.journalq.broker.consumer;

import com.jd.journalq.broker.consumer.model.ConsumePartition;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class PartitionLockInstanceTest {

    final PartitionLockInstance partitionLockInstance = new PartitionLockInstance();


    final String topic = "topic", app = "app";
    final short partition = 0;

    @Test
    public void getLockInstance() {
        ConsumePartition lockInstance = partitionLockInstance.getLockInstance(topic, app, partition);
        ConsumePartition lockInstance2 = partitionLockInstance.getLockInstance(topic, app, partition);

        Assert.assertEquals(lockInstance, lockInstance2);

        ConsumePartition lockInstance3 = partitionLockInstance.getLockInstance(topic, app, (short) 2);

        Assert.assertNotEquals(lockInstance, lockInstance3);
    }

    @Test
    public void getLockInstance1() {
        ConsumePartition consumePartition = new ConsumePartition();
        consumePartition.setPartition(partition);
        consumePartition.setTopic(topic);
        consumePartition.setApp(app);

        ConsumePartition consumePartition2 = new ConsumePartition();
        consumePartition2.setPartition(partition);
        consumePartition2.setTopic(topic);
        consumePartition2.setApp(app);

        ConsumePartition lockInstance = partitionLockInstance.getLockInstance(consumePartition);
        ConsumePartition lockInstance2 = partitionLockInstance.getLockInstance(consumePartition2);
        Assert.assertEquals(lockInstance, lockInstance2);

        ConsumePartition consumePartition3 = new ConsumePartition();
        consumePartition3.setPartition((short) 2);
        consumePartition3.setTopic(topic);
        consumePartition3.setApp(app);
        ConsumePartition lockInstance3 = partitionLockInstance.getLockInstance(consumePartition3);
        Assert.assertNotEquals(lockInstance, lockInstance3);
    }
}