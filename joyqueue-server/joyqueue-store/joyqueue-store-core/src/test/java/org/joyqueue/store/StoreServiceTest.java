/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.store;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.util.BaseDirUtils;
import org.joyqueue.store.utils.MessageUtils;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author liyue25
 * Date: 2018/9/10
 */
@Ignore
public class StoreServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(StoreServiceTest.class);
    private String topic = "raven_topic";

    private int partitionGroup = 6;
    private short[] partitions = new short[]{4, 5, 6};
    private Store store;

    @Before
    public void createStore() throws Exception {
        File base = BaseDirUtils.prepareBaseDir();
        logger.info("Base directory: {}.", base.getCanonicalPath());
        int MB = 1024 * 1024;
        long GB = 1024 * MB;

        StoreConfig config = new StoreConfig(null);
        config.setPath(base.getAbsolutePath());


        store = new Store(config);
        store.start();
        store.createPartitionGroup(topic, partitionGroup, partitions);
        store.getReplicableStore(topic, partitionGroup).enable();


    }

    @After
    public void destroyStore() throws IOException {
        if (null != store) {
            store.stop();
            store.close();
            store.physicalDelete();
        }
    }

    @Test
    public void performanceTest() throws Exception {
        PartitionGroupStore partitionGroupStore = store.getStore(topic, partitionGroup, QosLevel.RECEIVE);

        long count = 1024;
        int batchSize = 1024;
        int bodySize = 1024;
        long t0 = SystemClock.now();
        for (int i = 0; i < count; i++) {
            ByteBuffer[] messages = MessageUtils.build(batchSize, bodySize).toArray(new ByteBuffer[0]);
            short partition = partitions[ThreadLocalRandom.current().nextInt(partitions.length)];
            Future<WriteResult> future = partitionGroupStore.asyncWrite(Arrays.stream(messages).map(b -> new WriteRequest(partition, b)).toArray(WriteRequest[]::new));
            WriteResult writeResult = future.get();
            Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());
        }
        long t1 = SystemClock.now();
        long totalSize = count * batchSize * bodySize / 1024 / 1024;
        logger.info("Total write : {} MB, takes {} ms, average {} MBps.", totalSize, t1 - t0, totalSize * 1000 / (t1 - t0));

    }


}
