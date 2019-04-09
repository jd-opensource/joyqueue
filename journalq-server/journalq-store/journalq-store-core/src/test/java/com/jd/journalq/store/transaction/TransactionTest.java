package com.jd.journalq.store.transaction;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.store.WriteResult;
import com.jd.journalq.store.file.PositioningStore;
import com.jd.journalq.store.utils.BaseDirUtils;
import com.jd.journalq.store.utils.MessageTestUtils;
import com.jd.journalq.store.utils.PreloadBufferPool;
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
        PreloadBufferPool bufferPool = new PreloadBufferPool( );

        TransactionStoreManager transactionStoreManager = new TransactionStoreManager(base, config, bufferPool);
        int tId = transactionStoreManager.next();
        List<String> bodyList = MessageTestUtils.createBodyList("hahahaha", count);
        List<ByteBuffer> messages = MessageTestUtils.createMessages(bodyList);
        Future<WriteResult> future = transactionStoreManager.asyncWrite(tId, messages.stream().map(ByteBuffer::slice).toArray(ByteBuffer[]::new));
        WriteResult writeResult = future.get();
        Assert.assertEquals(JMQCode.SUCCESS, writeResult.getCode());

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