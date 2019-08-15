package io.chubao.joyqueue.broker.kafka.message.compressor;

import io.chubao.joyqueue.broker.kafka.message.exception.UnknownCodecException;

/**
 * KafkaCompressionCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/6
 */
public enum KafkaCompressionCodec {

    NoCompressionCodec(0, "none"),
    GZIPCompressionCodec(1, "gzip"),
    SnappyCompressionCodec(2, "snappy"),
    LZ4CompressionCodec(3, "lz4");

    private int code;
    private String name;

    KafkaCompressionCodec(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public static KafkaCompressionCodec valueOf(int codec) {
        for (KafkaCompressionCodec compressionCodec : KafkaCompressionCodec.values()) {
            if (compressionCodec.getCode() == codec) {
                return compressionCodec;
            }
        }
        throw new UnknownCodecException(String.format("%s is an unknown compression codec", codec));
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
}
