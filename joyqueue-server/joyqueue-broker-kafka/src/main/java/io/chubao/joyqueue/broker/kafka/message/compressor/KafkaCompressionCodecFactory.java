package io.chubao.joyqueue.broker.kafka.message.compressor;

import io.chubao.joyqueue.broker.kafka.message.compressor.lz4.KafkaLZ4BlockInputStream;
import io.chubao.joyqueue.broker.kafka.message.compressor.lz4.KafkaLZ4BlockOutputStream;
import io.chubao.joyqueue.broker.kafka.message.exception.UnknownCodecException;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * KafkaCompressionCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/9
 */
public final class KafkaCompressionCodecFactory {

    public static OutputStream apply(KafkaCompressionCodec codec, OutputStream stream, byte messageMagic) throws IOException {
        switch (codec) {
            case GZIPCompressionCodec:
                return new GZIPOutputStream(stream);
            case SnappyCompressionCodec:
                return new SnappyOutputStream(stream);
            case LZ4CompressionCodec:
                return new KafkaLZ4BlockOutputStream(stream);
            default:
                throw new UnknownCodecException(String.format("unknown codec: %s", codec));
        }
    }

    public static InputStream apply(KafkaCompressionCodec codec, InputStream stream, byte messageMagic) throws IOException {
        switch (codec) {
            case GZIPCompressionCodec:
                return new GZIPInputStream(stream);
            case SnappyCompressionCodec:
                return new SnappyInputStream(stream);
            case LZ4CompressionCodec:
                return new KafkaLZ4BlockInputStream(stream, messageMagic == 0); // RecordBatch.MAGIC_VALUE_V0
            default:
                throw new UnknownCodecException(String.format("unknown codec: %s", codec));
        }
    }
}
