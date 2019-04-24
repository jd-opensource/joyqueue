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
package com.jd.journalq.broker.consumer.filter;

import com.jd.journalq.exception.JournalqException;
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
    final String rule = "[1,2]";

    @Test
    public void setRule() {
        flagFilter.setRule(rule);
    }

    @Test
    public void filter() throws JournalqException {
        setRule();

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        // List<ByteBuffer> byteBufferList, FilterCallback filterCallback
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JournalqException {
                Assert.assertEquals(1, list.size());
            }
        });

        Assert.assertEquals(2, filter.size());
    }

    @Test
    public void filter1() throws JournalqException {
        flagFilter.setRule("[0,1,2]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        // List<ByteBuffer> byteBufferList, FilterCallback filterCallback
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JournalqException {
                Assert.assertEquals(null, list);
            }
        });

        Assert.assertEquals(3, filter.size());
    }

    @Test
    public void filter2() throws JournalqException {
        flagFilter.setRule("[9]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        // List<ByteBuffer> byteBufferList, FilterCallback filterCallback
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JournalqException {
                Assert.assertEquals(9, list.size());
            }
        });

        Assert.assertEquals(1, filter.size());
    }
}