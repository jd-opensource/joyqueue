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
package org.joyqueue.broker.kafka.message.compressor;

import org.joyqueue.broker.kafka.message.exception.UnknownCodecException;

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
