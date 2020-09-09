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
package org.joyqueue.broker.consumer.filter;

import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.exception.JoyQueueException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class FlagFilterTest {

    final FlagFilter flagFilter = new FlagFilter();

    @Test
    public void filter1() throws JoyQueueException {
        flagFilter.setRule("[0,1,2]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }

        boolean[] inCallback = {false};
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JoyQueueException {
                inCallback[0] = true;
            }
        });

        Assert.assertEquals(3, filter.size());
        Assert.assertEquals(false, inCallback[0]);
    }

    @Test
    public void filter2() throws JoyQueueException {
        flagFilter.setRule("[9]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 1; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }

        boolean[] inCallback = {false};
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JoyQueueException {
                Assert.assertEquals(8, list.size());
                for (int i = 0; i < 8; i++) {
                    Assert.assertEquals(i + 1, Serializer.readFlag(list.get(i)));
                }
                inCallback[0] = true;
            }
        });

        Assert.assertEquals(1, filter.size());
        Assert.assertEquals(true, inCallback[0]);
    }

    @Test
    public void filter3() throws JoyQueueException {
        flagFilter.setRule("[5, 9]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 1; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }

        int[] inCallback = {0};
        List<ByteBuffer> filter1 = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JoyQueueException {
                inCallback[0]++;
                Assert.assertEquals(4, list.size());
                for (int i = 0; i < 4; i++) {
                    Assert.assertEquals(i + 1, Serializer.readFlag(list.get(i)));
                }
            }
        });

        Assert.assertEquals(1, filter1.size());
        Assert.assertEquals(5, Serializer.readFlag(filter1.get(0)));
        Assert.assertEquals(1, inCallback[0]);

        List<ByteBuffer> filter2 = flagFilter.filter(byteBufferList.subList(6, 9), new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JoyQueueException {
                inCallback[0]++;
                Assert.assertEquals(2, list.size());
                for (int i = 0; i < 2; i++) {
                    Assert.assertEquals(i + 7, Serializer.readFlag(list.get(i)));
                }
            }
        });

        Assert.assertEquals(1, filter2.size());
        Assert.assertEquals(9, Serializer.readFlag(filter2.get(0)));
        Assert.assertEquals(2, inCallback[0]);
    }

    @Test
    public void filter4() throws JoyQueueException {
        flagFilter.setRule("[a]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JoyQueueException {
                Assert.assertEquals(10, list.size());
                for (int i = 0; i < 10; i++) {
                    Assert.assertEquals(i, Serializer.readFlag(list.get(i)));
                }
            }
        });

        Assert.assertEquals(1, filter.size());
    }
}