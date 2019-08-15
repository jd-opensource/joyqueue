package io.chubao.joyqueue.store;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2018/10/18
 */
public class WriteRequest implements Closeable {
    private final short partition;
    private ByteBuffer buffer;
    private int batchSize;

    public WriteRequest(short partition, ByteBuffer buffer) {
        this.partition = partition;
        this.buffer = buffer;
    }

    public WriteRequest(short partition, ByteBuffer buffer, int batchSize) {
        this.partition = partition;
        this.buffer = buffer;
        this.batchSize = batchSize;
    }

    public short getPartition() {
        return partition;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {

    }
}
