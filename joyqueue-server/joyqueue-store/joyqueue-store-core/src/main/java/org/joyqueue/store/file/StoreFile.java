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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 带缓存页的，存储数据的文件
 * @param <T> 数据类型
 */
public interface StoreFile<T> extends Timed {
    /**
     * 对应的文件
     */
    File file();

    /**
     * 文件起始全局位置
     */
    long position();

    /**
     * 卸载文件缓存页
     *
     * @return 成功返回true，如果文件存在脏数据返回false。
     */
    boolean unload();

    /**
     * 强制接卸，丢弃可能存在的脏数据
     */
    void forceUnload();

    /**
     * 缓存页是否已加载
     */
    boolean hasPage();

    /**
     * 用给定的位置和长度读取数据
     *
     * @param position 文件内的相对位置
     * @param length   数据长度，当长度小于0时则自动判断数据长度
     */
    T read(int position, int length) throws IOException;

    /**
     * 追加写入数据
     *
     * @param t 待写入的数据
     * @return 写入后文件的当前位置
     */
    int append(T t) throws IOException;

    /**
     * 读取一段ByteBuffer
     *
     * @param position 位置
     * @param length   长度
     */
    ByteBuffer readByteBuffer(int position, int length) throws IOException;

    /**
     * 写入一段Bytebuffer
     * @return 写入后文件的当前位置
     */
    int appendByteBuffer(ByteBuffer byteBuffer) throws IOException;

    /**
     * 将内存中的数据刷盘写入磁盘中
     *
     * @return 本次刷盘数据的长度
     */
    int flush() throws IOException;

    /**
     * 回滚到指定位置，未刷盘的数据直接丢弃，已刷盘的数据截断。
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

    /**
     * 调用fsync，确保数据写入到磁盘上。
     * @throws IOException 发生IO异常时抛出
     */
    void force() throws IOException;

    /**
     * 结束写入，文件变为只读。
     */
    void closeWrite();

    /**
     * 文件的最大容量。
     * 
     * @return 文件最大容量。
     */
    int capacity();
}
