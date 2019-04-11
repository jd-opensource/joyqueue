package com.jd.journalq.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * from: https://gist.github.com/bmchae/1344404 by Burhan Uddin
 *
 * @author modified by liyue25
 * Date: 2018/10/10
 */
public class StoreLock {
    private static final Logger logger = LoggerFactory.getLogger(StoreLock.class);
    private final File lockFile;
    private FileChannel channel;
    private FileLock lock;

    public StoreLock(File lockFile) {
        this.lockFile = lockFile;
    }

    public void lock() throws IOException {
        // Try to get the lock
        channel = new RandomAccessFile(lockFile, "rw").getChannel();
        lock = channel.tryLock();
        if (lock == null) {
            // File is lock by other application
            channel.close();
            throw new StoreLockedException();
        }
        // Add shutdown hook to release lock when application shutdown

        Runtime.getRuntime().addShutdownHook(new Thread(this::unlock));

    }

    private void unlock() {
        // release and delete file lock
        try {
            if (lock != null) {
                lock.release();
                channel.close();
                lockFile.delete();
            }
        } catch (IOException e) {
            logger.warn("Unlock file {} failed!", lockFile.getAbsoluteFile());
        }
    }
}
