package io.chubao.joyqueue.store.index;

import io.chubao.joyqueue.store.file.LogSerializer;

import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2018-11-28
 */
public class IndexSerializer implements LogSerializer<IndexItem> {


    @Override
    public IndexItem read(ByteBuffer buffer, int length) {
        return IndexItem.from(buffer);
    }

    @Override
    public int size(IndexItem indexItem) {
        return IndexItem.STORAGE_SIZE;
    }

    @Override
    public int trim(ByteBuffer byteBuffer, int length) {
        return byteBuffer.remaining() - byteBuffer.remaining() % IndexItem.STORAGE_SIZE;
    }

    @Override
    public int append(IndexItem indexItem, ByteBuffer to) {
        indexItem.serializeTo(to);
        return IndexItem.STORAGE_SIZE;
    }

}
