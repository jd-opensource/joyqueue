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
package org.joyqueue.toolkit.io;

import org.joyqueue.toolkit.lang.Close;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * 重要的小文件，双写，避免写文件时程序以外退出文件损坏。
 */
public abstract class DoubleCopy implements Closeable {


    // 第二份数据的位置
    private final int NEXT;

    // 时间戳
    private long timestamp;
    // 文件
    protected File file;
    private RandomAccessFile raf;
    private static Logger logger = LoggerFactory.getLogger(DoubleCopy.class);


    protected abstract String getName();

    protected abstract byte [] serialize();

    protected abstract void parse(byte [] data);

    protected int next() {
        return NEXT;
    }

    /**
     * 构造函数
     *
     * @param file 本地存储文件
     */
    public DoubleCopy(File file, int maxDataSize) throws IOException {

        this.file = file;
        NEXT = maxDataSize;
        validate();
    }

    protected void validate() throws IOException {
        String path = file.getParent();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!Files.createFile(file)) {
            throw new IOException(String.format("create file error,%s", file.getPath()));
        }
        if (!file.canWrite()) {
            throw new IOException(String.format("file can not be written,%s", file.getPath()));
        }
        if (!file.canRead()) {
            throw new IOException(String.format("file can not be read,%s", file.getPath()));
        }
        if (raf == null) {
            raf = new RandomAccessFile(file, "rw");
        }
    }

    @Override
    public void close() {
        doFlush();
        Close.close(raf);
        raf = null;
        logger.info(getName() + " is stopped.");
    }

    /**
     * 恢复
     *
     */
    public synchronized void recover() throws IOException {
        long length = raf.length();
        if (length > 0) {
            // 读取第一份数据
            raf.seek(0);

            // 读取第一份数据
            boolean success = false;
            try {
                success = tryToRecover();
            } catch (Exception e) {
                logger.warn("Exception while recover first copy of " + getName(), e);
            }


            if (!success) {
                try {
                    raf.seek(next());
                    success = tryToRecover();
                } catch (Exception e) {
                    logger.warn("Exception while recover second copy of " + getName(), e);
                }
            }

            if (!success) {
                throw new IOException(String.format("Recover file %s failed!", getName()));
            }
        }
        logger.info(getName() + " recover success.");
    }


    private boolean tryToRecover() throws IOException {
        int length = raf.readInt();
        long timestamp = raf.readLong();
        long checksum = raf.readLong();

        byte [] data = new byte [length];

        int size = raf.read(data, 0, length);

        if(size == length) {
            if(checksum == getChecksum(data)) {
                // 数据正常
                this.timestamp = timestamp;
                parse(data);
                return true;
            }

        }
        return false;
    }


    /**
     * 写入到磁盘中
     */
    private void doFlush() {
        byte [] data = serialize();
        int length = data.length;
        long timestamp = SystemClock.now();
        long checksum = getChecksum(data);

        try {
            // 双写
            raf.seek(0);
            raf.writeInt(length);
            raf.writeLong(timestamp);
            raf.writeLong(checksum);
            raf.write(data);

            raf.seek(next());
            raf.writeInt(length);
            raf.writeLong(timestamp);
            raf.writeLong(checksum);
            raf.write(data);

            raf.getFD().sync();
        } catch (IOException e) {
            logger.error(getName() + "flush error.", e);
        } finally {
            this.timestamp = timestamp;
        }
    }

    /**
     * 刷盘
     */
    public synchronized void flush() {
            doFlush();
    }

    public long getTimestamp() {
        return timestamp;
    }

    private long getChecksum(byte [] data){
        Checksum crc = new CRC32();
        crc.update(data, 0, data.length);
        return crc.getValue();
    }


}