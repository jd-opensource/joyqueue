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
import org.joyqueue.store.ReadException;
import org.joyqueue.store.StoreInitializeException;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 管理暂存的未提交的事务消息
 *
 * @author liyue25
 * Date: 2018/10/10
 */
public class TransactionStoreManager implements TransactionStore, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(TransactionStoreManager.class);
    private final File base;
    private final AtomicInteger idSequence;
    private final ExecutorService writeExecutor;
    private final Map<Integer, PositioningStore<ByteBuffer>> storeMap;
    private final PositioningStore.Config config;
    private final PreloadBufferPool bufferPool;

    public TransactionStoreManager(File base, PositioningStore.Config config, PreloadBufferPool bufferPool) {
        this.base = base;
        this.config = config;
        this.bufferPool = bufferPool;
        idSequence = new AtomicInteger(0);
        loadFiles();

        writeExecutor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1024), new ThreadPoolExecutor.AbortPolicy());
        storeMap = new HashMap<>();

    }

    private void loadFiles() {
        if (!base.isDirectory()) {
            throw new StoreInitializeException(
                    String.format("Init transaction store directory failed! " +
                            "Directory NOT exists: %s!", base.getAbsolutePath()));
        }

        File[] files = base.listFiles();
        if (files != null) {
            idSequence.set(
                    Arrays.stream(files)
                            .filter(File::isDirectory)
                            .map(File::getName)
                            .filter(fileName -> fileName.matches("\\d+"))
                            .mapToInt(Integer::parseInt)
                            .max().orElse(-1)
            );
            idSequence.incrementAndGet();
        }


    }

    private PositioningStore<ByteBuffer> get(int id) {
        synchronized (storeMap) {
            return storeMap.get(id);
        }
    }


    private PositioningStore<ByteBuffer> getOrCreate(int id) throws IOException {
        synchronized (storeMap) {
            if (!storeMap.containsKey(id)) {
                File storeBase = new File(base, String.valueOf(id));
                if (storeBase.exists() || storeBase.mkdir()) {
                    PositioningStore<ByteBuffer> store = new PositioningStore<>(storeBase, config, bufferPool, new TransactionMessageSerializer());
                    storeMap.put(id, store);
                    return store;
                } else {
                    throw new IOException(String.format("Failed to create directory: %s.", storeBase.getAbsolutePath()));
                }
            } else {
                return storeMap.get(id);
            }
        }
    }

    private WriteResult write(int id, List<ByteBuffer> messages) {
        WriteResult writeResult = new WriteResult();
        try {
            PositioningStore<ByteBuffer> positioningStore = getOrCreate(id);
            positioningStore.append(messages);
            positioningStore.flush();
            writeResult.setCode(JoyQueueCode.SUCCESS);
        } catch (Throwable t) {
            logger.warn("Write transaction file \"{}/{}\" exception: ", base.getAbsoluteFile(), id, t);
            writeResult.setCode(JoyQueueCode.CN_TRANSACTION_EXECUTE_ERROR);
        }
        return writeResult;
    }

    /**
     * 获取下一个事务ID
     */
    @Override
    public int next() {
        return idSequence.getAndIncrement();
    }

    /**
     * 列出所有进行中的事务ID
     */
    @Override
    public int[] list() {

        File[] files = base.listFiles();
        if (files != null) {

            return Arrays.stream(files)
                    .map(File::getName)
                    .filter(fileName -> fileName.matches("\\d+"))
                    .mapToInt(Integer::parseInt)
                    .sorted()
                    .toArray();
        }

        return new int[0];

    }

    /**
     * 删除事务
     */
    @Override
    public boolean remove(int id) {
        synchronized (storeMap) {
            PositioningStore<ByteBuffer> store = storeMap.remove(id);
            if (null != store) store.close();
        }
        File txFile = new File(base, String.valueOf(id));
        try {
            return deleteFolder(txFile);
        } catch (IOException e) {
            logger.warn("Exception: ", e);
            return false;
        }
    }

    /**
     * 异步写入消息，线程安全，保证ACID(写入磁盘)
     *
     * @param id       事务id
     * @param messages 消息
     * @return 以Future形式返回结果
     * @see WriteResult
     */
    @Override
    public Future<WriteResult> asyncWrite(int id, ByteBuffer... messages) {
        return writeExecutor.submit(new WriteTask(id, Arrays.asList(messages)));
    }

    /**
     * 获取读取的迭代器
     */
    @Override
    public Iterator<ByteBuffer> readIterator(int id) {
        PositioningStore<ByteBuffer> store = get(id);
        return store == null ? null : new ReadIterator(store);
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     */
    @Override
    public void close() {
        long timeout = 5000, t0 = SystemClock.now();
        writeExecutor.shutdown();

        while (!writeExecutor.isTerminated() && SystemClock.now() - t0 < timeout) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(100));
            } catch (InterruptedException ignored) {
            }
        }
        if (!writeExecutor.isTerminated()) {
            logger.warn("Failed to shutdown executor!");
        }


        storeMap.values().forEach(PositioningStore::close);

    }

    private boolean deleteFolder(File folder) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if (!f.delete()) {
                        throw new IOException(String.format("Can not delete file: %s", f.getAbsolutePath()));
                    }
                }
            }
        }
        return folder.delete();
    }

    private class WriteTask implements Callable<WriteResult> {
        private final int id;
        private final List<ByteBuffer> messages;

        WriteTask(int id, List<ByteBuffer> messages) {
            this.id = id;
            this.messages = messages;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         */
        @Override
        public WriteResult call() {
            return write(id, messages);
        }
    }

    private class ReadIterator implements Iterator<ByteBuffer> {

        private final PositioningStore<ByteBuffer> store;
        private final long right;
        private long position;

        private ReadIterator(PositioningStore<ByteBuffer> store) {
            this.store = store;
            right = store.right();
            position = 0;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return position < right;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         * @throws ReadException          文件读取异常
         */
        @Override
        public ByteBuffer next() {
            if (hasNext()) {
                ByteBuffer buffer;
                try {
                    buffer = store.read(position);
                    position += buffer.remaining();
                    return buffer;
                } catch (Throwable t) {
                    throw new ReadException(t);
                }

            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
