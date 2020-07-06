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
package org.joyqueue.network.serializer;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dingjun
 * @since 16-5-5.
 */
public class JoyQueueMapTools {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    //public static final byte NULL_TYPE=Byte.MAX_VALUE;
    public static final byte BYTE_TYPE = 1;
    public static final byte SHORT_TYPE = 2;
    public static final byte INT_TYPE = 4;
    public static final byte LONG_TYPE = 8;
    public static final byte DOUBLE_TYPE = 7;
    public static final byte STRING_BYTE_TYPE = 9;
    public static final byte STRING_SHORT_TYPE = 10;
    public static final byte STRING_INT_TYPE = 11;
    public static final byte BYTE_ARRAY_INT_TYPE = 12;

    // 读取Map数据
    public static Map<Object, Object> readMap(final ByteBuf in) throws Exception {
        int size = in.readInt();
        byte type;
        Object key;
        Object value;
        Map<Object, Object> hashMap = new HashMap<Object, Object>();
        if (0 == size) {
            return hashMap;
        } else {
            for (int i = 0; i < size; i++) {
                type = in.readByte();
                key = readObject(type, in);
                type = in.readByte();
                value = readObject(type, in);
                if (key != null && value != null) {
                    hashMap.put(key, value);
                }
            }

        }
        return hashMap;
    }

     // 根据类型读取数据
    public static Object readObject(byte type, ByteBuf in) {
        Object value = null;
        Object mRetObject;
        switch (type) {
            case BYTE_TYPE:
                value = in.readByte();
                break;
            case SHORT_TYPE:
                value = in.readShort();
                break;
            case INT_TYPE:
                value = in.readInt();
                break;
            case LONG_TYPE:
                value = in.readLong();
                break;
            case DOUBLE_TYPE:
                value = in.readDouble();
                break;
            case STRING_BYTE_TYPE:
                value = readByteString(in);
                break;
            case STRING_SHORT_TYPE:
                value = readShortString(in);
                break;
            case STRING_INT_TYPE:
                value = readIntString(in);
                break;
            default:
                throw new RuntimeException("type is invalid:" + type);
        }
        mRetObject = value;
        return mRetObject;
    }

    private static String readByteString(ByteBuf in) {
        byte len = in.readByte();
        return readString(in, len);
    }

    private static String readShortString(ByteBuf in) {
        short len = in.readShort();
        return readString(in, len);
    }

    private static String readIntString(ByteBuf in) {
        int len = in.readInt();
        return readString(in, len);
    }

    private static String readString(ByteBuf in, int len) {
        if (len == 0) {
            return "";
        }
        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        return new String(bytes, UTF8);
    }

     //写入map数据
    public static <K, V> void write(final Map<K, V> hashMap, ByteBuf out) throws Exception {
        int size = hashMap.size();
        out.writeInt(size);
        if (0 == size) {
            return;
        }
        Iterator iterator = hashMap.entrySet().iterator();
        Map.Entry entry;
        while (iterator.hasNext()) {
            entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
                write(key, out);
                write(value, out);
            } else if (value == null) {
                throw new RuntimeException("map value can't be null for serializing,key:" + key);
            }
        }
    }


     // 根据类型写入
    public static void write(final Object object, final ByteBuf out) {
        if (object instanceof Byte) {
            out.writeByte(BYTE_TYPE);
            out.writeByte(((Byte) object).byteValue());
        } else if (object instanceof Short) {
            out.writeByte(SHORT_TYPE);
            out.writeShort(((Short) object).shortValue());
        } else if (object instanceof Integer) {
            out.writeByte(INT_TYPE);
            out.writeInt(((Integer) object).intValue());
        } else if (object instanceof Long) {
            out.writeByte(LONG_TYPE);
            out.writeLong(((Long) object).longValue());
        } else if (object instanceof Double) {
            out.writeByte(DOUBLE_TYPE);
            out.writeDouble((Double) object);
        } else if (object instanceof String) {
            String str = (String) object;
            byte[] bytes = str.getBytes(UTF8);
            int size = bytes.length;
            if (size <= Byte.MAX_VALUE) {
                out.writeByte(STRING_BYTE_TYPE);
                out.writeByte((byte) size);
            } else if (size <= Short.MAX_VALUE) {
                out.writeByte(STRING_SHORT_TYPE);
                out.writeShort((short) size);
            } else if (size <= Integer.MAX_VALUE) {
                out.writeByte(STRING_INT_TYPE);
                out.writeInt(size);
            }
            out.writeBytes(bytes);
        } else {
            throw new RuntimeException("type is illegal:" + object);
        }
    }

    public static Byte getByte(Map<Object, Object> map, Object key) {
        Object obj = map.get(key);
        if (obj == null) {
            return Byte.valueOf((byte) 0);
        } else {
            return (Byte) obj;
        }
    }

    public static Short getShort(Map<Object, Object> map, Object key) {
        Object obj = map.get(key);
        if (obj == null) {
            return Short.valueOf((short) 0);
        } else {
            return (Short) obj;
        }
    }

    public static Integer getInt(Map<Object, Object> map, Object key) {
        Object obj = map.get(key);
        if (obj == null) {
            return Integer.valueOf(0);
        } else {
            return (Integer) obj;
        }
    }

    public static Long getLong(Map<Object, Object> map, Object key) {
        Object obj = map.get(key);
        if (obj == null) {
            return Long.valueOf(0);
        } else {
            return (Long) obj;
        }
    }

    public static String getString(Map<Object, Object> map, Object key) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        } else {
            return (String) obj;
        }
    }
}
