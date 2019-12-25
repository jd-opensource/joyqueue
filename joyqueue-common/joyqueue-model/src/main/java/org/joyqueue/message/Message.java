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

import org.joyqueue.toolkit.io.Compressors;
import org.joyqueue.toolkit.io.Snappy;
import org.joyqueue.toolkit.io.Zip;
import org.joyqueue.toolkit.io.Zlib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 *
 * @author lining11
 * Date: 2018/8/17
 */
public class Message implements Serializable {

    public static final String TAGS = "TAGS";
    public static final int COMPRESS_THRESHOLD = 100;
    public static String EXPIRE = "EXPIRE";

    protected static final Logger logger = LoggerFactory.getLogger(Message.class);

    // 索引ID
    protected short partition = -1;
    // 主题
    protected String topic;
    // 标签, 如果是批量消息代表批量条数
    protected short flag;
    // 应用
    protected String app;
    // 业务ID
    protected String businessId;

    // 优先级
    protected byte priority;
    // 是否压缩
    protected boolean compressed;
    // 压缩算法 (默认ZIP)
    protected CompressionType compressionType;
    // 顺序消息
    protected boolean ordered;

    // 文本
    protected String text;
    // 消息体校验码
    protected long bodyCRC;
    // 消息体
    protected byte[] body;
    // 属性
    protected Map<String, String> attributes;

    protected String txId;

    @Deprecated
    public Message() {
    }

    /**
     * 构造函数
     *
     * @param topic      主题
     * @param text       文本
     * @param businessId 业务ID
     */
    @Deprecated
    public Message(String topic, String text, String businessId) {
        setTopic(topic);
        setBusinessId(businessId);
        setText(text);
    }

    /**
     * Constructor
     * @param topic      主题
     * @param text       文本
     * @param businessId 业务ID
     * @param compressionType 压缩方式
     */
    @Deprecated
    public Message(String topic, String text, String businessId, CompressionType compressionType) {
        setTopic(topic);
        setBusinessId(businessId);
        setCompressionType(compressionType);
        setText(text);
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public Message topic(final String topic) {
        setTopic(topic);
        return this;
    }

    public Message flag(final short flag) {
        setFlag(flag);
        return this;
    }

    public Message app(final String app) {
        setApp(app);
        return this;
    }

    public Message businessId(final String businessId) {
        setBusinessId(businessId);
        return this;
    }

    public Message priority(final byte priority) {
        setPriority(priority);
        return this;
    }

    public Message ordered(final boolean ordered) {
        setOrdered(ordered);
        return this;
    }

    public Message text(final String text) {
        setText(text);
        return this;
    }

    public Message body(final byte[] data) {
        setBody(data, 0, data.length);
        return this;
    }

    public Message attribute(final String key, final String value) {
        setAttribute(key, value);
        return this;
    }

    public Message attributes(final Map<String, String> attributes) {
        setAttributes(attributes);
        return this;
    }

    public Message partition(final short partition){
        setPartition(partition);
        return this;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }




    public short getFlag() {
        return this.flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getBusinessId() {
        return this.businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public byte getPriority() {
        return this.priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public boolean isOrdered() {
        return this.ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public boolean isCompressed() {
        return this.compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public CompressionType getCompressionType() {
        if (!isCompressed()) {
            return CompressionType.none;
        }
        return compressionType;
    }

    /**
     * compress only works when data length larger than 100
     * @param compressionType Zip / Snappy
     */
    public void setCompressionType(CompressionType compressionType) {
        this.compressionType = compressionType;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getBodyCRC() {
//        if (bodyCRC == 0) {
//            if (body != null && body.length > 0) {
//                Checksum checksum = new Adler32();
//                checksum.update(body,0,body.length);
//                bodyCRC = checksum.getValue();
//            }
//        }
        return bodyCRC;
    }

    public void setBodyCRC(long bodyCRC) {
        this.bodyCRC = bodyCRC;
    }

    public ByteBuffer getBody() {
        return ByteBuffer.wrap(body);
    }

    public void setBody(ByteBuffer buf) {
        if (buf.hasArray()) {
            byte[] data = buf.array();
            int offset = 0;
            int length = 0;
            offset = buf.arrayOffset() + buf.position();
            length = buf.remaining();
            byte[] dest = new byte[length];
            System.arraycopy(data,offset,dest,0,length);
            setBody(dest);
        } else {
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            setBody(data);
        }
    }

    public byte[] getByteBody(){
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
        CRC32 crc32 = new CRC32();
        crc32.update(body);
        this.bodyCRC = crc32.getValue();
    }

    public void setBody(byte[] data, int offset, int length) {
        if (offset == 0 && length == data.length) {
            setBody(data);
        } else {
            int srcLen = data.length;
            int remain = length;
            if (remain > srcLen - offset) {
                remain = srcLen - offset;
            }
            byte[] dest = new byte[remain];
            System.arraycopy(data, offset, dest, 0, remain);
            setBody(dest);
        }
    }

    public String getText() {
        if (text == null && body != null) {

            try {
                if (compressed) {
                    byte[] data = null;
                    switch (compressionType) {
                        case Zip: {
                            data = Compressors.decompress(body, 0, body.length, Zip.INSTANCE);
                            break;
                        }
                        case ZLIB: {
                            data = Compressors.decompress(body, 0, body.length, Zlib.INSTANCE);
                            break;
                        }
                        case Snappy: {
                            data = Compressors.decompress(body, 0, body.length, Snappy.INSTANCE);
                            break;
                        }
                    }
                    text = new String(data, Charset.forName("UTF-8"));
                } else {
                    text = new String(body, Charset.forName("UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("decode body error.", e);
            } catch (IOException e) {
                throw new IllegalStateException("decompress body error.", e);
            }
        }
        return text;
    }

    /**
     * 返回解压后的body，如果没有压缩返回原body
     * @return body
     */
    public byte[] getDecompressedBody() {
        try {
            if (compressed) {
                switch (compressionType) {
                    case none: {
                        return body;
                    }
                    case Zip: {
                        return Compressors.decompress(body, 0, body.length, Zip.INSTANCE);
                    }
                    case Snappy: {
                        return Compressors.decompress(body, 0, body.length, Snappy.INSTANCE);
                    }
                    case ZLIB: {
                        return Compressors.decompress(body, 0, body.length, Zlib.INSTANCE);
                    }
                }
                return body;
            } else {
                return body;
            }
        } catch (Exception e) {
            logger.error("getDecompressedBody error, topic: {}, app: {}", topic, app, e);
            return body;
        }
    }

    @Deprecated
    public void setText(String text) {
        this.text = text;

        byte[] data;
        compressed = false;
        if (text == null) {
            data = new byte[0];
        } else {
            data = text.getBytes(Charset.forName("UTF-8"));
        }
        if (data.length >= COMPRESS_THRESHOLD) {
            try {
                data = Compressors.compress(data, 0, data.length,
                        compressionType != CompressionType.Snappy ? Zip.INSTANCE : Snappy.INSTANCE);
                compressed = true;
            } catch (IOException ignored) {
            }
        }
        body = data;
        bodyCRC = 0;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(key);
    }

    public void setAttribute(String key, String value) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        attributes.put(key, value);
    }

    public String getTags() {
        return getAttribute(TAGS);
    }

    public void setTags(String tags) {
        setAttribute(TAGS, tags);
    }

    public long getExpire() {
        String value = getAttribute(EXPIRE);
        if (value == null) {
            return 0;
        }
        return Long.valueOf(value);
    }

    public void setExpire(long expire) {
        setAttribute(EXPIRE, String.valueOf(expire));
    }

    public int getSize() {
        if (body != null) {
            return body.length;
        } else if (text != null) {
            byte[] bytes = text.getBytes(Charset.forName("UTF-8"));
            return bytes.length;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;

        if (bodyCRC != message.bodyCRC) {
            return false;
        }
        if (compressed != message.compressed) {
            return false;
        }
        if (flag != message.flag) {
            return false;
        }
        if (ordered != message.ordered) {
            return false;
        }
        if (priority != message.priority) {
            return false;
        }
        if (app != null ? !app.equals(message.app) : message.app != null) {
            return false;
        }
        if (attributes != null ? !attributes.equals(message.attributes) : message.attributes != null) {
            return false;
        }
        if (body != null ? !body.equals(message.body) : message.body != null) {
            return false;
        }
        if (businessId != null ? !businessId.equals(message.businessId) : message.businessId != null) {
            return false;
        }
        if (topic != null ? !topic.equals(message.topic) : message.topic != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (compressed ? 1 : 0);
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + (int) flag;
        result = 31 * result + (app != null ? app.hashCode() : 0);
        result = 31 * result + (businessId != null ? businessId.hashCode() : 0);
        result = 31 * result + (int) priority;
        result = 31 * result + (ordered ? 1 : 0);
        result = 31 * result + (int) (bodyCRC ^ (bodyCRC >>> 32));
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    /**
     * Compressors for message.
     * This is exposed to users for settings.
     */
    public enum CompressionType {

        none(0),

        Zip(1),

        ZLIB(2),

        Snappy(3),

        ;

        private int type;

        CompressionType(int type) {
            this.type = type;
        }

        public static CompressionType valueOf(int value) {
            switch (value) {
                case 0:
                    return none;
                case 1:
                    return Zip;
                case 2:
                    return ZLIB;
                case 3:
                    return Snappy;
                default: {
                    throw new UnsupportedOperationException("unsupported type, type: " + value);
                }
            }
        }

        public static CompressionType convert(String value) {
            for (CompressionType compressionType : values()) {
                if (compressionType.name().equalsIgnoreCase(value)) {
                    return compressionType;
                }
            }
            return null;
        }

        public int getType() {
            return type;
        }
    }
}