package com.jd.journalq.store.utils;

import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.toolkit.security.Crc32;

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
    public static ByteBuffer createMessage(String body){
        return createMessage(body.getBytes());
    }
    public static ByteBuffer createMessage(byte [] body){
        byte [] biz_id = new byte[8];
        Arrays.fill(biz_id,(byte) 0x25);
        byte [] property = "This is property!".getBytes(StandardCharsets.UTF_8);
        byte [] expand = "This is expand!".getBytes(StandardCharsets.UTF_8);
        byte [] app = new byte[8];
        Arrays.fill(app,(byte) 0x21);

        byte [][] varAtts = {body, biz_id, property, expand, app};
        ByteBuffer byteBuffer = MessageParser.build(varAtts);
        MessageParser.setLong(byteBuffer, MessageParser.CLIENT_TIMESTAMP, System.currentTimeMillis());
        Crc32 crc32 = new Crc32();
        crc32.update(body);
        MessageParser.setLong(byteBuffer, MessageParser.CRC, crc32.getValue());

        return byteBuffer;
    }


    public static List<ByteBuffer> createMessages(List<String> bodyList) {

        return   bodyList.stream()
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
