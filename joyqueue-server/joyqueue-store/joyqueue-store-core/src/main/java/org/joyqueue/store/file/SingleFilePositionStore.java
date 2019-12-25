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

import org.joyqueue.store.ReadException;
import org.joyqueue.toolkit.buffer.RByteBuffer;
import org.joyqueue.toolkit.lang.Close;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author liyue25
 * Date: 2018/10/11
 */
public class SingleFilePositionStore implements Closeable {
    private static final byte RS = 0x1E;
    private static final ByteBuffer RS_BUFF = (ByteBuffer) ByteBuffer.wrap(new byte[]{RS}).mark();
    private final int fileHeaderSize;
    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel fileChannel;

    public SingleFilePositionStore(File file, int fileHeaderSize) throws IOException {
        this.fileHeaderSize = fileHeaderSize;
        this.file = file;
        raf = new RandomAccessFile(file, "rws");
        fileChannel = raf.getChannel();
        if (fileChannel.position() < fileHeaderSize) fileChannel.position(fileHeaderSize);
    }

    public synchronized void append(RByteBuffer... rByteBuffers) throws IOException {
        long position = -1L;
        try {
            position = fileChannel.position();
            for (RByteBuffer buff :
                    rByteBuffers) {
                appendOne(buff);
            }
            fileChannel.force(false);
        } catch (Throwable t) {
            fileChannel.truncate(position);
            throw t;
        }
    }

    private void appendOne(RByteBuffer byteBuffer) throws IOException {
        try {
            fileChannel.write(byteBuffer.getBuffer());
            fileChannel.write((ByteBuffer) RS_BUFF.reset());
        } finally {
            if (null != byteBuffer) byteBuffer.close();
        }
    }

    /**
     * 读取一条消息
     *
     * @param position 消息的起始位置
     */
    public RByteBuffer get(long position) throws IOException {
        int length = readMessageLength(position);
        if (length > Integer.BYTES) {
            // 读一条消息
            RByteBuffer rb = new RByteBuffer(ByteBuffer.allocate(length + 1), null);
            int readLength = read(rb.getBuffer(), position);

            // 检查读到的消息长度
            if (readLength != length + 1) {
                throw new ReadException(
                        String.format("Message length check failed after read. " +
                                        "Expect: %d, actual: %d, " +
                                        "store: %s, position: %d.",
                                length + 1, rb.remaining(), file.getAbsolutePath()
                                , position));
            }
            rb.flip();

            // 检查末尾的分隔符
            if (RS != rb.get(rb.position() + length)) {
                throw new ReadException(
                        String.format("Message should be end with char:RS(0x1E). " +
                                        "Store: %s, position: %d.",
                                file.getAbsolutePath(), position));
            }
            rb.limit(rb.limit() - 1); // 返回的数据不含分隔符
            return rb;
        }
        return null;
    }

    private int readMessageLength(long position) throws IOException {
        ByteBuffer rb = ByteBuffer.allocate(Integer.BYTES);
        int length = read(rb, position);
        rb.flip();
        return length == Integer.BYTES ? rb.getInt() : -1;

    }

    private int read(ByteBuffer buffer, long position) throws IOException {
        return fileChannel.read(buffer, absPosition(position));
    }

    private long absPosition(long position) {
        return fileHeaderSize + position;
    }

    @Override
    public synchronized void close() {
        Close.close(fileChannel);
        Close.close(raf);
    }

    public long length() throws IOException {
        return fileChannel.position() - fileHeaderSize;
    }
}
