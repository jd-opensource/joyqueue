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
package org.joyqueue.broker.consumer.converter.kafka;

import org.joyqueue.broker.consumer.converter.kafka.compressor.lz4.KafkaLZ4BlockInputStream;
import org.joyqueue.broker.consumer.converter.kafka.compressor.lz4.KafkaLZ4BlockOutputStream;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * KafkaCompressionCodecFactory
 *
 * author: gaohaoxiang
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
