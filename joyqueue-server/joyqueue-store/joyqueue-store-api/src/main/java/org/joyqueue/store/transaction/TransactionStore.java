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

import org.joyqueue.store.WriteResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Future;

/**
 * 缓存事务消息的可靠存储
 */
public interface TransactionStore {


    /**
     * 获取下一个事务ID
     */
    int next();

    /**
     * 列出所有进行中的事务ID
     * @return 返回从小到大有序的事务ID
     */
    int [] list();

    /**
     * 删除事务
     */
    boolean remove(int id);
    /**
     * 异步写入消息，线程安全，保证ACID(写入磁盘)
     * @param id 事务id
     * @param messages 消息
     * @return 以Future形式返回结果
     * @see WriteResult
     */
    Future<WriteResult> asyncWrite(int id, ByteBuffer... messages);

    /**
     * 获取读取的迭代器。
     * 迭代器内包含获取迭代器时store包含的消息。
     * 注意：迭代器无法读取到获取迭代器之后新写入的消息。
     * @param id 事务ID
     * @return 事务迭代器，如果事务不存在则返回null
     */
    Iterator<ByteBuffer> readIterator(int id) throws IOException;



}
