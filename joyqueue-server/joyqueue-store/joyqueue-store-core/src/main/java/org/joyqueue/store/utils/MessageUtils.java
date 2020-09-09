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
package org.joyqueue.store.utils;

import org.joyqueue.store.message.BatchMessageParser;
import org.joyqueue.store.message.MessageParser;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.CRC32;

public class MessageUtils {
    static byte[] b1024;
    private static byte[] bid = new byte[]{0, 0, 0, 0};
    private static byte[] prop = new byte[]{0, 0, 0, 0};
    private static byte[] expand = new byte[]{0, 0, 0, 0};
    private static byte[] app = new byte[]{0, 0, 0, 0};

    static {
        byte[] body = new byte[1024];
        byte[][] varAtts = {body, bid, prop, expand, app};
        ByteBuffer byteBuffer = MessageParser.build(varAtts);
        CRC32 crc32 = new CRC32();
        crc32.update(body);
        MessageParser.setLong(byteBuffer, MessageParser.CRC, crc32.getValue());
        b1024 = byteBuffer.array();
    }

    public static List<ByteBuffer> build(int count, int bodyLength) {
        byte[] body = new byte[bodyLength];
        return IntStream.range(0, count).mapToObj(i -> {
            Arrays.fill(body, (byte) (i % Byte.MAX_VALUE));
            byte[][] varAtts = {body, bid, prop, expand, app};
            ByteBuffer byteBuffer = MessageParser.build(varAtts);
            CRC32 crc32 = new CRC32();
            crc32.update(body);
            MessageParser.setLong(byteBuffer, MessageParser.CRC, crc32.getValue());
            return byteBuffer;
        }).collect(Collectors.toList());
    }

    public static List<ByteBuffer> buildBatch(short count, int bodyLength) {
        byte[] body = new byte[bodyLength];
        return IntStream.range(0, count).mapToObj(i -> {
            Arrays.fill(body, (byte) (i % Byte.MAX_VALUE));
            byte[][] varAtts = {body, bid, prop, expand, app};
            ByteBuffer byteBuffer = MessageParser.build(varAtts);
            CRC32 crc32 = new CRC32();
            crc32.update(body);
            MessageParser.setLong(byteBuffer, MessageParser.CRC, crc32.getValue());
            MessageParser.setShort(byteBuffer, MessageParser.FLAG, count);
            return byteBuffer;
        }).collect(Collectors.toList());
    }

    public static ByteBuffer[] build1024(int count) {

        return IntStream.range(0, count).parallel().mapToObj(i -> ByteBuffer.wrap(Arrays.copyOf(b1024, b1024.length))).toArray(ByteBuffer[]::new);
    }

    public static ByteBuffer toBatchMessage(ByteBuffer msg, short batchSize) {
        BatchMessageParser.setBatch(msg, true);
        BatchMessageParser.setBatchSize(msg, batchSize);
        return msg;
    }

    public static ByteBuffer build1024() {
        return ByteBuffer.wrap(Arrays.copyOf(b1024, b1024.length));
    }

    public static void main(String[] args) {
        List<ByteBuffer> buffers = build(1, 1024);
        System.out.println(MessageParser.getString(buffers.get(0)));
    }
}
