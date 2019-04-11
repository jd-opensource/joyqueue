/**
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
package com.jd.journalq.toolkit.buffer.bytes;

import com.jd.journalq.toolkit.buffer.ByteArray;

import java.nio.ByteOrder;

/**
 * 操作内存或磁盘字节数组的接口
 */
public interface Bytes extends BytesInput, BytesOutput<Bytes> {
    int SIZE_4K = 1024 * 4;
    int BYTE = 1;
    int BOOLEAN = 1;
    int CHARACTER = 2;
    int SHORT = 2;
    int MEDIUM = 3;
    int INTEGER = 4;
    int LONG = 8;
    int FLOAT = 4;
    int DOUBLE = 8;

    /**
     * 字节数
     *
     * @return 字节数
     */
    long size();

    /**
     * 修改大小
     * <p>
     * 修改大小后，拷贝的该实例内存地址也许无效了。此外，如果新尺寸比当前小，则可能引起数据丢失。
     *
     * @param newSize 新的大小
     * @return 修改后的对象
     */
    Bytes resize(long newSize);

    /**
     * 返回字节顺序
     * <p>
     * For consistency with {@link java.nio.ByteBuffer}, all bytes implementations are initially in
     * {@link java.nio.ByteOrder#BIG_ENDIAN} order.
     *
     * @return The byte order.
     */
    ByteOrder order();

    /**
     * Sets the byte order, returning a new swapped {@link Bytes} instance.
     * <p>
     * By default, all bytes are read and written in {@link java.nio.ByteOrder#BIG_ENDIAN} order. This provides complete
     * consistency with {@link java.nio.ByteBuffer}. To flip bytes to {@link java.nio.ByteOrder#LITTLE_ENDIAN} order,
     * this
     * {@code Bytes} instance is decorated by a {@link SwappedBytes} instance which will reverse
     * read and written bytes using, e.g. {@link Integer#reverseBytes(int)}.
     *
     * @param order The byte order.
     * @return The updated bytes.
     * @throws NullPointerException If the {@code order} is {@code null}
     */
    Bytes order(ByteOrder order);

    /**
     * 堆外内存标示
     *
     * @return 堆外内存标示
     */
    boolean isDirect();

    /**
     * 是否是文件.
     *
     * @return 文件标示.
     */
    boolean isFile();

    /**
     * 返回字节数组
     *
     * @param read 读模式
     * @return 字节数组
     */
    ByteArray array(boolean read);

    void close();

}
