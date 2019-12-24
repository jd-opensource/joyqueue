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
package org.joyqueue.message;

import org.joyqueue.toolkit.network.IpUtil;

import java.util.Arrays;

/**
 * 消息ID
 */
public class MessageId {

    // 地址
    private byte[] address;
    // 偏移量
    private long offset;
    // 消息ID
    private String messageId;

    public MessageId(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            throw new IllegalArgumentException("messageId is invalid");
        }
        int pos = messageId.indexOf('-');
        if (pos < 0) {
            throw new IllegalArgumentException("messageId is invalid");
        }
        String[] parts = new String[2];
        parts[0] = messageId.substring(0, pos);
        parts[1] = messageId.substring(pos + 1);
        if (parts[0] == null || parts[0].isEmpty() || parts[1] == null || parts[1].isEmpty()) {
            throw new IllegalArgumentException("messageId is invalid");
        }
        offset = Long.valueOf(parts[1]);
        if (offset < 0) {
            throw new IllegalArgumentException("messageId is invalid");
        }
        address = IpUtil.toByte(parts[0]);
    }

    public MessageId(byte[] address, long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset is invalid");
        }
        if (address == null || address.length < 4) {
            throw new IllegalArgumentException("address is invalid");
        }
        this.address = address;
        this.offset = offset;
    }

    public byte[] getAddress() {
        return this.address;
    }

    public long getOffset() {
        return this.offset;
    }

    public String getMessageId() {
        if (messageId == null && address != null) {
            StringBuilder sb = new StringBuilder();
            toHex(address, sb);
            sb.append('-').append(offset);
            messageId = sb.toString();
        }
        return this.messageId;
    }

    public String toString() {
        return getMessageId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageId messageId1 = (MessageId) o;

        if (offset != messageId1.offset) {
            return false;
        }
        if (!Arrays.equals(address, messageId1.address)) {
            return false;
        }
        if (messageId != null ? !messageId.equals(messageId1.messageId) : messageId1.messageId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? Arrays.hashCode(address) : 0;
        result = 31 * result + (int) (offset ^ (offset >>> 32));
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        return result;
    }


    private void toHex(byte[] address, StringBuilder builder) {
        if (address == null || address.length == 0 || builder == null) {
            return;
        }
        String hex;
        int pos = 0;
        int port = 0;
        int length = address.length;
        boolean hasPort = length == 6 || length == 18;
        // 有端口
        if (hasPort) {
            port = address[pos++] & 0xFF;
            port |= (address[pos++] << 8 & 0xFF00);
        }
        // IP段
        for (int i = 0; i < (!hasPort?length:length-2); i++) {
            hex = Integer.toHexString(address[pos++] & 0xFF).toUpperCase();
            if (hex.length() == 1) {
                builder.append('0').append(hex);
            } else {
                builder.append(hex);
            }
        }
        // 追加端口字符串
        if (hasPort) {
            hex = Integer.toHexString(port).toUpperCase();
            int len = hex.length();
            if (len == 1) {
                builder.append("000").append(hex);
            } else if (len == 2) {
                builder.append("00").append(hex);
            } else if (len == 3) {
                builder.append("0").append(hex);
            } else {
                builder.append(hex);
            }
        }
    }
}