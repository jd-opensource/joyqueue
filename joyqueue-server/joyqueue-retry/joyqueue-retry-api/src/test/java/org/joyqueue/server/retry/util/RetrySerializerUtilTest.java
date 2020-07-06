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
package org.joyqueue.server.retry.util;

import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.server.retry.util.RetrySerializerUtil;
import org.joyqueue.toolkit.time.SystemClock;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class RetrySerializerUtilTest {

    @Test
    public void deserialize() {
        serialize();
    }

    @Test
    public void serialize() {
        RetryMessageModel retry = new RetryMessageModel();
        retry.setBusinessId("business");
        retry.setTopic("topic");
        retry.setApp("app");
        retry.setPartition((short) 255);
        retry.setIndex(100L);
        retry.setBrokerMessage(new byte[168]);
        retry.setException(new byte[16]);
        retry.setSendTime(SystemClock.now());

        ByteBuffer serialize = RetrySerializerUtil.serialize(retry);

        RetryMessageModel model = RetrySerializerUtil.deserialize(ByteBufAllocator.DEFAULT.buffer().writeBytes(serialize.array()));

        Assert.assertEquals(model.getBusinessId(), retry.getBusinessId());
        Assert.assertEquals(model.getTopic(), retry.getTopic());
        Assert.assertEquals(model.getApp(), retry.getApp());
        Assert.assertEquals(model.getPartition(), retry.getPartition());
        Assert.assertEquals(model.getIndex(), retry.getIndex());
        Assert.assertEquals(model.getSendTime(), retry.getSendTime());

        Assert.assertArrayEquals(model.getBrokerMessage(), retry.getBrokerMessage());
        Assert.assertArrayEquals(model.getException(), retry.getException());
    }
}