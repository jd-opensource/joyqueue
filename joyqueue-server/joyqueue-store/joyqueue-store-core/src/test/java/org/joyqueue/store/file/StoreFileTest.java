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
package org.joyqueue.store.file;

import org.joyqueue.store.utils.MessageTestUtils;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.toolkit.util.BaseDirUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author majun8
 */
public class StoreFileTest {
    private static final Logger logger = LoggerFactory.getLogger(StoreFileTest.class);
    private File base = null;
    private File file = null;
    private long timestamp = -1L;

    @Before
    public void before() throws Exception {
        prepareBaseDir();
    }

    @After
    public void after() throws Exception {
        destroyBaseDir();
    }

    @Test
    public void timestampTest() throws IOException {
        long start = SystemClock.now();
        StoreFileImpl<ByteBuffer> storeFile = new StoreFileImpl<>(666L, base, 128, new StoreMessageSerializer(1024), PreloadBufferPool.getInstance(), 1024 * 1024 * 10, false, false);
        storeFile.append(MessageTestUtils.createMessage(new byte[10]));
        storeFile.flush();
        long timestamp = storeFile.timestamp();
//        long timestamp = SystemClock.now();
        long end = SystemClock.now();
        logger.info("Start: {}, timestamp: {}", start, timestamp);
        Assert.assertTrue(start <= timestamp);
        Assert.assertTrue(timestamp <= end);


        storeFile.unload();

        storeFile = new StoreFileImpl<>(666L, base, 128, new StoreMessageSerializer(1024), PreloadBufferPool.getInstance(), 1024 * 1024 * 10, false, false);

        Assert.assertEquals(timestamp, storeFile.timestamp());

    }


    @Test
    public void readFileNotExistTimestamp() {
        ByteBuffer timeBuffer = ByteBuffer.allocate(8);
        try (RandomAccessFile raf = new RandomAccessFile(new File(base, "fileNotExist"), "r"); FileChannel fileChannel = raf.getChannel()) {
            fileChannel.position(0);
            fileChannel.read(timeBuffer);
        } catch (Exception e) {
            logger.error("Error to read timestamp from file: <{}> header, error: <{}>", file.getAbsolutePath(), e.getMessage());
        } finally {
            try {
                timestamp = timeBuffer.getLong(0);
            } catch (IndexOutOfBoundsException iobe) {
                logger.error("Error to read timestamp long value from file: <{}> header, error: <{}>", file.getAbsolutePath(), iobe.getMessage());
            }
        }
        logger.info("read timestamp: {}", timestamp);
    }

    @Test
    public void readTimestamp() {
        ByteBuffer timeBuffer = ByteBuffer.allocate(8);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
            fileChannel.position(0);
            fileChannel.read(timeBuffer);
        } catch (Exception e) {
            logger.error("Error to read timestamp from file: <{}> header, error: <{}>", file.getAbsolutePath(), e.getMessage());
        } finally {
            try {
                timestamp = timeBuffer.getLong(0);
            } catch (IndexOutOfBoundsException iobe) {
                logger.error("Error to read timestamp long value from file: <{}> header, error: <{}>", file.getAbsolutePath(), iobe.getMessage());
            }
        }
        logger.info("read timestamp: {}", timestamp);
    }

    @Test
    public void writeTimestamp() {
        ByteBuffer timeBuffer = ByteBuffer.allocate(8);
        long creationTime = SystemClock.now();
        timeBuffer.putLong(0, creationTime);
        //timeBuffer.flip();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
            fileChannel.position(0);
            fileChannel.write(timeBuffer);
            fileChannel.force(true);
        } catch (Exception e) {
            logger.error("Error to write timestamp from file: <{}> header, error: <{}>", file.getAbsolutePath(), e.getMessage());
        } finally {
            timestamp = creationTime;
        }
        logger.info("write timestamp: {}", timestamp);
    }

    private void prepareBaseDir() throws IOException {
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        base = new File(tempDir + File.separator + "joyqueue-data");
        if (!base.exists()) {
            base.mkdirs();
        }
        logger.info("Base directory: {}.", base.getCanonicalPath());
        file = new File(base, "329369803896");
    }


    private void destroyBaseDir() {
        BaseDirUtils.destroyBaseDir(base);
        base = null;
    }

}
