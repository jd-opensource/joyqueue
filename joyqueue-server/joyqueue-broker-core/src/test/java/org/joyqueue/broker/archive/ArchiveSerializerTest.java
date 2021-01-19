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
package org.joyqueue.broker.archive;

import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by chengzhiliang on 2018/12/13.
 */
public class ArchiveSerializerTest {


    @Test
    public void write() {
        ConsumeLog consumeLog = new ConsumeLog();
        consumeLog.setApp("app_test");
        consumeLog.setBrokerId(1);
        consumeLog.setClientIp(new byte[16]);
        consumeLog.setBytesMessageId(new byte[16]);
        consumeLog.setConsumeTime(SystemClock.now());

        System.out.println(ToStringBuilder.reflectionToString(consumeLog));

        ByteBuffer write = ArchiveSerializer.write(consumeLog);
        System.out.println(write.limit());
    }

    @Test
    public void read() {
        ConsumeLog consumeLog = new ConsumeLog();
        consumeLog.setApp("app_test");
        consumeLog.setBrokerId(1);
        consumeLog.setClientIp(new byte[16]);
        consumeLog.setBytesMessageId(new byte[16]);
        consumeLog.setConsumeTime(SystemClock.now());
        System.out.println(ToStringBuilder.reflectionToString(consumeLog));

        ByteBuffer write = ArchiveSerializer.write(consumeLog);

        int readLen = write.getInt();
        System.out.println(readLen);
        ConsumeLog log = ArchiveSerializer.read(write);
        System.out.println(ToStringBuilder.reflectionToString(log));

    }

    @Test
    public void reverseStringTest() {
        String test = "abc";
        Assert.assertEquals("cba",
                org.joyqueue.server.archive.store.utils.ArchiveSerializer.reverse(test));
    }

    @Test
    public void reverseBytesTest() {
        String test = "abc";
        byte[] bytes = test.getBytes();
        Assert.assertEquals(new String(bytes),
                new String(org.joyqueue.server.archive.store.utils.ArchiveSerializer.reverse(
                        org.joyqueue.server.archive.store.utils.ArchiveSerializer.reverse(bytes)
                ))
        );
    }
}