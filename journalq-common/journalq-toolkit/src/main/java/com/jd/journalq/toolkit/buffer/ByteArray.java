package com.jd.journalq.toolkit.buffer;

/**
 * 字节数组对象
 * Created by hexiaofeng on 16-7-21.
 */
public class ByteArray {
    // 字节数组
    private byte[] array;
    // 数据偏移量
    private int offset;
    // 数据长度
    private int length;

    public ByteArray(byte[] array) {
        this(array, 0, array.length);
    }

    public ByteArray(byte[] array, int offset, int length) {
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public byte[] array() {
        return array;
    }

    public int offset() {
        return offset;
    }

    public int length() {
        return length;
    }
}
