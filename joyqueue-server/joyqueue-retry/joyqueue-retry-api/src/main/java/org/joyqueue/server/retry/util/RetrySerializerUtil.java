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
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 重试序列化工具列
 * <p>
 * Created by chengzhiliang on 2019/2/10.
 */
public class RetrySerializerUtil {

    public static RetryMessageModel deserialize(ByteBuf buffer) {
        RetryMessageModel retryMessageModel = new RetryMessageModel();

        short buzIdLen = buffer.readShort();
        if (buzIdLen > 0) {
            byte[] buzId = new byte[buzIdLen];
            buffer.readBytes(buzId);
            String businessId = new String(buzId, Charset.forName("UTF-8"));
            retryMessageModel.setBusinessId(businessId);
        }

        short topicLen = buffer.readShort();
        byte[] topicBytes = new byte[topicLen];
        buffer.readBytes(topicBytes);
        String topic = new String(topicBytes, Charset.forName("UTF-8"));
        retryMessageModel.setTopic(topic);

        short appLen = buffer.readShort();
        byte[] appBytes = new byte[appLen];
        buffer.readBytes(appBytes);
        String app = new String(appBytes, Charset.forName("UTF-8"));
        retryMessageModel.setApp(app);

        short partition = buffer.readShort();
        retryMessageModel.setPartition(partition);

        long index = buffer.readLong();
        retryMessageModel.setIndex(index);

        int messageLen = buffer.readInt();
        byte[] messageBytes = new byte[messageLen];
        buffer.readBytes(messageBytes);
        retryMessageModel.setBrokerMessage(messageBytes);

        short exceptionLen = buffer.readShort();
        if (exceptionLen > 0) {
            byte[] exceptionBytes = new byte[exceptionLen];
            buffer.readBytes(exceptionBytes);
            retryMessageModel.setException(exceptionBytes);
        }

        long sendTime = buffer.readLong();
        retryMessageModel.setSendTime(sendTime);

        return retryMessageModel;
    }

    public static ByteBuffer serialize(RetryMessageModel retryMessageModel) {
        int size = size(retryMessageModel);
        ByteBuffer allocate = ByteBuffer.allocate(size);

        String businessId = retryMessageModel.getBusinessId();
        if (StringUtils.isNotEmpty(businessId)) {
            byte[] bytes = businessId.getBytes(Charset.forName("UTF-8"));
            allocate.putShort((short) bytes.length);
            allocate.put(bytes);
        } else {
            allocate.putShort((short) 0);
        }

        String topic = retryMessageModel.getTopic();
        if (StringUtils.isNotEmpty(topic)) {
            byte[] bytes = topic.getBytes(Charset.forName("UTF-8"));
            allocate.putShort((short) bytes.length);
            allocate.put(bytes);
        } else {
            allocate.putShort((short)0);
        }

        String app = retryMessageModel.getApp();
        if (StringUtils.isNotEmpty(app)) {
            byte[] bytes = app.getBytes(Charset.forName("UTF-8"));
            allocate.putShort((short) bytes.length);
            allocate.put(bytes);
        } else {
            allocate.putShort((short)0);
        }

        short partition = retryMessageModel.getPartition();
        allocate.putShort(partition);

        long index = retryMessageModel.getIndex();
        allocate.putLong(index);

        byte[] brokerMessage = retryMessageModel.getBrokerMessage();
        allocate.putInt(brokerMessage.length);
        allocate.put(brokerMessage);

        byte[] exception = retryMessageModel.getException();
        if (exception != null) {
            allocate.putShort((short)exception.length);
            allocate.put(exception);
        } else {
            allocate.putShort((short)0);
        }

        long sendTime = retryMessageModel.getSendTime();
        allocate.putLong(sendTime);

        allocate.flip();

        return allocate;
    }

    private static int size(RetryMessageModel retryMessageModel) {
        // 2个字节业务Id长度、2个字节主题长度、2个字节应用长度、4个字节消息长度、2个字节异常信息长度
        int messageSize = 2 + 2 + 2 + 4 + 2;

        String businessId = retryMessageModel.getBusinessId();
        if (StringUtils.isNotEmpty(businessId)) {
            messageSize += businessId.getBytes(Charset.forName("UTF-8")).length;
        }

        String topic = retryMessageModel.getTopic();
        if (StringUtils.isNotEmpty(topic)) {
            messageSize += topic.getBytes(Charset.forName("UTF-8")).length;
        }

        String app = retryMessageModel.getApp();
        if (StringUtils.isNotEmpty(app)) {
            messageSize += app.getBytes(Charset.forName("UTF-8")).length;
        }

        //short partition = retryMessageModel.getPartition();
        messageSize += 2;

        // long index = retryMessageModel.getIndex();
        messageSize += 8;

        byte[] brokerMessage = retryMessageModel.getBrokerMessage();
        if (brokerMessage != null) {
            messageSize += brokerMessage.length;
        }

        byte[] exception = retryMessageModel.getException();
        if (exception != null) {
            messageSize += exception.length;
        }

        // long sendTime = retryMessageModel.getEndTime();
        messageSize += 8;

        return messageSize;
    }

}
