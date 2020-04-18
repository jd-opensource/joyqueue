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
package org.joyqueue.store.transaction;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.utils.MessageTestUtils;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.util.BaseDirUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author liyue25
 * Date: 2018-12-21
 */
public class TransactionTest {
    private static final Logger logger = LoggerFactory.getLogger(TransactionTest.class);
    private File base = null;

    @Test
    public void transactionTest() throws Exception {
        int count = 1024;
        PositioningStore.Config config = new PositioningStore.Config();
        PreloadBufferPool bufferPool = PreloadBufferPool.getInstance();

        TransactionStoreManager transactionStoreManager = new TransactionStoreManager(base, config, bufferPool);
        int tId = transactionStoreManager.next();
        List<String> bodyList = MessageTestUtils.createBodyList("hahahaha", count);
        List<ByteBuffer> messages = MessageTestUtils.createMessages(bodyList);
        Future<WriteResult> future = transactionStoreManager.asyncWrite(tId, messages.stream().map(ByteBuffer::slice).toArray(ByteBuffer[]::new));
        WriteResult writeResult = future.get();
        Assert.assertEquals(JoyQueueCode.SUCCESS, writeResult.getCode());

        Iterator<ByteBuffer> iterator = transactionStoreManager.readIterator(tId);
        int i = 0;
        while (iterator.hasNext()) {
            ByteBuffer rByteBuffer = iterator.next();
            Assert.assertEquals(messages.get(i++), rByteBuffer);
        }

        Assert.assertEquals(count, i);


    }


    @After
    public void destroyBaseDir() {
        BaseDirUtils.destroyBaseDir(base);

        base = null;
    }

    @Before
    public void prepareBaseDir() throws IOException {

        base = BaseDirUtils.prepareBaseDir();
        logger.info("Base directory: {}.", base.getCanonicalPath());

    }
}