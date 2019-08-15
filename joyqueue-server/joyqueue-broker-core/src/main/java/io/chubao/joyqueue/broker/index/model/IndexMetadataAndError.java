package io.chubao.joyqueue.broker.index.model;


/**
 * Created by zhuduohui on 2018/9/17.
 */
public class IndexMetadataAndError {
    private long index;
    private String metadata;
    private short error;


    public IndexMetadataAndError(long index, String metadata, short error) {

        this.index = index;
        this.metadata = metadata;
        this.error = error;
    }

    public long getIndex() {
        return index;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("IndexMetadataAndError[%d,%s,%d]",index, metadata, error);
    }

}
