package io.chubao.joyqueue.store.message;

import java.nio.ByteBuffer;

/**
 * 批消息解析器
 * @author liyue25
 * Date: 2019-02-22
 */
public class BatchMessageParser {
    /**
     * 批消息标志位在消息中第几个字节（Byte）
     */
    private static final int BATCH_FLAG_BYTE_INDEX = MessageParser.SYS;
    /**
     * 批消息标志位在字节(Byte)中第几位(Bit, 从高位(左）向低位（右）数，起始位置为0)
     */
    private static final int BATCH_FLAG_BIT_INDEX = 4;
    private static final int BATCH_SIZE_INDEX = MessageParser.FLAG;
    public static boolean isBatch(ByteBuffer msg) {
        return 1 == MessageParser.getBit(msg, BATCH_FLAG_BYTE_INDEX, BATCH_FLAG_BIT_INDEX);

    }

    public static void setBatch(ByteBuffer msg, boolean isBatch) {
        MessageParser.setBit(msg, BATCH_FLAG_BYTE_INDEX, BATCH_FLAG_BIT_INDEX, isBatch);

    }

    public static short getBatchSize(ByteBuffer msg) {
        return MessageParser.getShort(msg, BATCH_SIZE_INDEX);
    }
    public static void setBatchSize(ByteBuffer msg, short batchSize) {
        MessageParser.setShort(msg, BATCH_SIZE_INDEX, batchSize);
    }
}
