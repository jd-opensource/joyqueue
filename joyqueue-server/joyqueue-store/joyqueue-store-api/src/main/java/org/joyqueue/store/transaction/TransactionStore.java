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

import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.toolkit.concurrent.EventListener;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 缓存事务消息的可靠存储
 */
public interface TransactionStore {


    /**
     * 开启一个新事务，并返回事务ID。
     */
    StoreTransactionContext createTransaction(Map<String, String> context) throws ExecutionException, InterruptedException;

    /**
     * 列出所有进行中的事务ID
     * @return 返回从小到大有序的事务ID
     */
    Collection<StoreTransactionContext> getOpeningTransactions() throws ExecutionException, InterruptedException;

    /**
     * 结束事务，可能是提交或者回滚事务。
     * @param transactionId 事务ID
     * @param commitOrAbort true：提交事务，false：回滚事务。
     */
    void completeTransaction(StoreTransactionId transactionId, boolean commitOrAbort) throws ExecutionException, InterruptedException;


    /**
     * 异步写入消息，线程安全，保证ACI，D的保证取决于WriteQosLevel
     * @param transactionId 事务ID
     * @param eventListener 回调方法，可以为null，表示不需要回调。
     * @param writeRequests partition序号和消息
     * @throws NullPointerException writeRequests为空时抛出
     * @see WriteResult
     * @see WriteRequest
     */
    void asyncWrite(StoreTransactionId transactionId, EventListener<WriteResult> eventListener, WriteRequest... writeRequests);

    /**
     * 异步写入消息，线程安全，保证ACI，D的保证取决于WriteQosLevel
     * @param transactionId 事务ID
     * @param writeRequests partition序号和消息
     * @return 以Future形式返回结果
     * @throws NullPointerException eventListener或writeRequests为空时抛出
     * @see WriteResult
     * @see WriteRequest
     */
    CompletableFuture<WriteResult> asyncWrite(StoreTransactionId transactionId, WriteRequest... writeRequests);



}
