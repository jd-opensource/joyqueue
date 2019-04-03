package com.jd.journalq.client.internal.consumer.converter.kafka.compressor;

import com.jd.journalq.client.internal.consumer.converter.kafka.compressor.lz4.KafkaLZ4BlockInputStream;
import com.jd.journalq.client.internal.consumer.converter.kafka.compressor.lz4.KafkaLZ4BlockOutputStream;
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

    public static OutputStream apply(KafkaCompressionCodec codec, OutputStream stream, byte messageVersion) throws IOException {
        switch (codec) {
            case GZIPCompressionCodec:
                return new GZIPOutputStream(stream);
            case SnappyCompressionCodec:
                return new SnappyOutputStream(stream);
            case LZ4CompressionCodec:
                return new KafkaLZ4BlockOutputStream(stream);
            default:
                throw new RuntimeException(String.format("unknown codec: %s", codec));
        }
    }

    public static InputStream apply(KafkaCompressionCodec codec, InputStream stream, byte messageVersion) throws IOException {
        switch (codec) {
            case GZIPCompressionCodec:
                return new GZIPInputStream(stream);
            case SnappyCompressionCodec:
                return new SnappyInputStream(stream);
            case LZ4CompressionCodec:
                return new KafkaLZ4BlockInputStream(stream, messageVersion == 0); // RecordBatch.MAGIC_VALUE_V0
            default:
                throw new RuntimeException(String.format("unknown codec: %s", codec));
        }
    }
}
