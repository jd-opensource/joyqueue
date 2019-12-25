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
package org.joyqueue.broker.archive;

import org.joyqueue.server.archive.store.model.ConsumeLog;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * 归档序列化工具类
 * <p>
 * Created by chengzhiliang on 2018/12/4.
 */
public class ArchiveSerializer {

    private static ByteBufferPool byteBufferPool = new ByteBufferPool();

    public static void release(ByteBuffer byteBuffer) {
        byteBufferPool.release(byteBuffer);
    }

    /**
     * 序列化消费日志
     *
     * @param consumeLog
     * @return
     */
    public static ByteBuffer write(ConsumeLog consumeLog) {
        int size = consumeLogSize(consumeLog);
        ByteBuffer buffer = byteBufferPool.acquire(/* 4 byte len */ 4 + size, false); // 4个字节长度
        buffer.putInt(size);
        buffer.put(consumeLog.getBytesMessageId());
        buffer.putInt(consumeLog.getBrokerId());

        byte[] clientIpBytes16 = new byte[16];
        byte[] clientIpBytes = consumeLog.getClientIp();
        System.arraycopy(clientIpBytes, 0, clientIpBytes16, 0, Math.min(clientIpBytes.length, clientIpBytes16.length));
        buffer.put(clientIpBytes16);

        buffer.putLong(consumeLog.getConsumeTime());

        byte[] appBytes = consumeLog.getApp().getBytes(Charset.forName("utf-8"));
        buffer.putShort((short) appBytes.length);
        buffer.put(appBytes);

        buffer.flip();

        return buffer;
    }

    /**
     * 计算消费日志占用的长度
     * <br>
     * （byte[]:messageId, int:brokerId, byte[16]:clientIP, long:consumeTime, String:app(变长), ）
     *
     * @param consumeLog
     * @return
     */
    private static int consumeLogSize(ConsumeLog consumeLog) {
        int size = 0;
        // messageId
        size += consumeLog.getBytesMessageId().length;
        // brokerId
        size += 4;
        // clientIp
        size += 16;
        // consumeTime
        size += 8;
        // app长度
        size += 2;
        // app
        size += consumeLog.getApp().getBytes(Charset.forName("utf-8")).length;
        return size;
    }

    /**
     * @param buffer
     * @return
     */
    public static ConsumeLog read(ByteBuffer buffer) {
        ConsumeLog log = new ConsumeLog();

        byte[] byteMessageId = new byte[16];
        buffer.get(byteMessageId);
        log.setBytesMessageId(byteMessageId);

        log.setBrokerId(buffer.getInt());

        byte[] clientIp = new byte[16];
        buffer.get(clientIp);
        log.setClientIp(clientIp);

        log.setConsumeTime(buffer.getLong());

        int appLen = (int)buffer.getShort();
        byte[] appBytes = new byte[appLen];
        buffer.get(appBytes);
        log.setApp(new String(appBytes, Charset.forName("utf-8")));

        return log;
    }


    /**
     * from jetty
     * <p>
     * ByteBuffer池
     */
    static class ByteBufferPool {

        private final ConcurrentMap<Integer, Queue<ByteBuffer>> directBuffers = new ConcurrentHashMap<>();
        private final ConcurrentMap<Integer, Queue<ByteBuffer>> heapBuffers = new ConcurrentHashMap<>();
        private final int factor;

        ByteBufferPool() {
            this(1024);
        }

        ByteBufferPool(int factor) {
            this.factor = factor;
        }

        public ByteBuffer acquire(int size, boolean direct) {
            int bucket = bucketFor(size);
            ConcurrentMap<Integer, Queue<ByteBuffer>> buffers = buffersFor(direct);

            ByteBuffer result = null;
            Queue<ByteBuffer> byteBuffers = buffers.get(bucket);
            if (byteBuffers != null)
                result = byteBuffers.poll();

            if (result == null) {
                int capacity = bucket * factor;
                result = newByteBuffer(capacity, direct);
            }

            result.clear();
            return result;
        }

        protected ByteBuffer newByteBuffer(int capacity, boolean direct) {
            return direct ? ByteBuffer.allocateDirect(capacity)
                    : ByteBuffer.allocate(capacity);
        }

        public void release(ByteBuffer buffer) {
            if (buffer == null) {
                return;
            }

            int bucket = bucketFor(buffer.capacity());
            ConcurrentMap<Integer, Queue<ByteBuffer>> buffers = buffersFor(buffer.isDirect());

            Queue<ByteBuffer> byteBuffers = buffers.get(bucket);
            if (byteBuffers == null) {
                byteBuffers = new ConcurrentLinkedQueue<>();
                Queue<ByteBuffer> existing = buffers.putIfAbsent(bucket, byteBuffers);
                if (existing != null)
                    byteBuffers = existing;
            }

            buffer.clear();
            byteBuffers.offer(buffer);
        }

        public void clear() {
            directBuffers.clear();
            heapBuffers.clear();
        }

        private int bucketFor(int size) {
            int bucket = size / factor;
            if (size % factor > 0)
                ++bucket;
            return bucket;
        }

        ConcurrentMap<Integer, Queue<ByteBuffer>> buffersFor(boolean direct) {
            return direct ? directBuffers : heapBuffers;
        }

    }
}
