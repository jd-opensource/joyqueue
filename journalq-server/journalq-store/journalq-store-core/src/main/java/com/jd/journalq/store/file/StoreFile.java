package com.jd.journalq.store.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface StoreFile<T> extends Timed {
    /**
     * 对应的问题
     */
    File file();

    /**
     * 文件起始全局位置
     */
    long position();

    /**
     * 卸载文件缓存页
     * @return 成功返回true，如果文件存在脏数据返回是吧
     */
    boolean unload();

    /**
     * 强制接卸，丢弃可能存在的脏数据
     */
    void forceUnload();

    /**
     * 是否有缓存页
     */
    boolean hasPage();

    /**
     * 用给定的位置和长度读取数据
     * @param position 文件内的相对位置
     * @param length 数据长度，当长度小于0时则自动判断数据长度
     */
    T read(int position, int length) throws IOException;

    /**
     * 追加写入数据
     * @param t 待写入的数据
     * @return 写入长度
     */
    int append(T t) throws IOException;

    /**
     * 读取一段ByteBuffer
     * @param position 位置
     * @param length 长度
     */
    ByteBuffer readByteBuffer(int position, int length) throws IOException;

    /**
     * 写入一段Bytebuffer
     */
    int appendByteBuffer(ByteBuffer byteBuffer) throws IOException;

    /**
     * 将内存中的数据写入磁盘中
     * @return 本次写入数据的大小
     */
    int flush() throws IOException;

    /**
     * 回滚到指定位置，未刷盘的数据直接丢弃，已刷盘的数据需要截断。
     */
    void rollback(int position) throws IOException;

    /**
     * 内存中的数据是否和磁盘一致
     */
    boolean isClean();

    /**
     * 写入位置
     */
    int writePosition();

    /**
     * 文件中数据大小（不含文件头）
     */
    int fileDataSize();

    /**
     * 刷盘位置
     */
    int flushPosition();

    /**
     * 文件创建时间
     */
    long timestamp();
}
