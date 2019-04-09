package com.jd.journalq.store.file;

import com.jd.journalq.store.utils.MessageTestUtils;
import com.jd.journalq.store.utils.PreloadBufferPool;
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

    @Test
    public void timestampTest() throws IOException {
        long start = System.currentTimeMillis();
        StoreFileImpl<ByteBuffer> storeFile = new StoreFileImpl<>(666L, base,128,new StoreMessageSerializer(1024),new PreloadBufferPool(), 1024 * 1024 * 10);
        long timestamp = storeFile.timestamp();
        long end = System.currentTimeMillis();
        Assert.assertTrue( start <= timestamp);
        Assert.assertTrue( timestamp <= end);

        storeFile.append(MessageTestUtils.createMessage(new byte[10]));
        storeFile.flush();
        storeFile.unload();

        storeFile = new StoreFileImpl<>(666L, base,128,new StoreMessageSerializer(1024),new PreloadBufferPool(), 1024 * 1024 * 10);

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
        long creationTime = System.currentTimeMillis();
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
        base = new File(tempDir + File.separator + "jmq-data");
        if (!base.exists()) {
            base.mkdirs();
        }
        logger.info("Base directory: {}.", base.getCanonicalPath());
        file = new File(base, "329369803896");
    }
}
