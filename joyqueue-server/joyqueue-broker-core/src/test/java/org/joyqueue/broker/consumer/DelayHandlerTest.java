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
package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.DelayHandler;
import org.joyqueue.domain.Consumer;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class DelayHandlerTest {

    private DelayHandler delayHandler = new DelayHandler();

    @Test
    public void handle() {
        // Consumer.ConsumerPolicy consumerPolicy, List<ByteBuffer> byteBufferList
        Consumer.ConsumerPolicy consumerPolicy = new Consumer.ConsumerPolicy();
        consumerPolicy.setDelay(1);

        List<ByteBuffer> byteBufferList = new LinkedList<>();

        ByteBuffer allocate = ByteBuffer.allocate(100);
        allocate.position(39);
        allocate.putLong(SystemClock.now() - 1000);
        allocate.flip();

        ByteBuffer allocate2 = ByteBuffer.allocate(100);
        allocate2.position(39);
        allocate2.putLong(SystemClock.now() + 1000);
        allocate2.flip();


        byteBufferList.add(allocate);
        byteBufferList.add(allocate2);

        List<ByteBuffer> handle = delayHandler.handle(consumerPolicy, byteBufferList);

        Assert.assertEquals(1, handle.size());

    }
}