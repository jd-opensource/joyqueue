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

import org.joyqueue.store.message.MessageParser;
import org.joyqueue.toolkit.security.Crc32;
import org.joyqueue.toolkit.time.SystemClock;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author liyue25
 * Date: 2018-11-28
 */
public class MessageTestUtils {
    public static ByteBuffer createMessage(String body) {
        return createMessage(body.getBytes());
    }

    public static ByteBuffer createMessage(byte[] body) {
        byte[] biz_id = new byte[8];
        Arrays.fill(biz_id, (byte) 0x25);
        byte[] property = "This is property!".getBytes(StandardCharsets.UTF_8);
        byte[] expand = "This is expand!".getBytes(StandardCharsets.UTF_8);
        byte[] app = new byte[8];
        Arrays.fill(app, (byte) 0x21);

        byte[][] varAtts = {body, biz_id, property, expand, app};
        ByteBuffer byteBuffer = MessageParser.build(varAtts);
        MessageParser.setLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP, SystemClock.now());
        Crc32 crc32 = new Crc32();
        crc32.update(body);
        MessageParser.setLong(byteBuffer, MessageParser.CRC, crc32.getValue());

        return byteBuffer;
    }


    public static List<ByteBuffer> createMessages(List<String> bodyList) {

        return bodyList.stream()
                .map(MessageTestUtils::createMessage)
                .collect(Collectors.toList());

    }


    public static List<String> createBodyList(String body, int count) {
        return IntStream.range(0, count).mapToObj(i -> body + i).collect(Collectors.toList());
    }


    public static List<String> getBodies(List<ByteBuffer> logs) {
        return logs.stream()
                .map(byteBuffer -> MessageParser.getBytes(byteBuffer, MessageParser.BODY))
                .map(String::new).collect(Collectors.toList());
    }
}
