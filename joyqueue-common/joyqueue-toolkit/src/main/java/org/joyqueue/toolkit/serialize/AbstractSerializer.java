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
package org.joyqueue.toolkit.serialize;

import org.joyqueue.toolkit.io.Compressors;
import org.joyqueue.toolkit.io.Zip;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author lining11
 * Date: 2018/9/17
 */
public abstract class AbstractSerializer {

    private static final char[] hexDigit =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};



    /**
     * 把Map转换成Properties字符串
     *
     * @param attributes 散列
     * @return 返回值 字符串
     */
    public static String toProperties(final Map<String, String> attributes) {
        if (attributes == null) {
            return "";
        }
        int count = 0;
        StringBuilder builder = new StringBuilder(100);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (count > 0) {
                builder.append('\n');
            }
            append(builder, entry.getKey(), true, true);
            builder.append('=');
            append(builder, entry.getValue(), false, true);
            count++;
        }
        return builder.toString();
    }

    /**
     * 添加字符串
     *
     * @param builder       缓冲区
     * @param value         字符串
     * @param escapeSpace   转移空格标示
     * @param escapeUnicode 转移Unicode标示
     */
    protected static void append(final StringBuilder builder, final String value, final boolean escapeSpace,
                               final boolean escapeUnicode) {
        int len = value.length();
        for (int x = 0; x < len; x++) {
            char aChar = value.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    builder.append('\\');
                    builder.append('\\');
                    continue;
                }
                builder.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        builder.append('\\');
                    }
                    builder.append(' ');
                    break;
                case '\t':
                    builder.append('\\');
                    builder.append('t');
                    break;
                case '\n':
                    builder.append('\\');
                    builder.append('n');
                    break;
                case '\r':
                    builder.append('\\');
                    builder.append('r');
                    break;
                case '\f':
                    builder.append('\\');
                    builder.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    builder.append('\\');
                    builder.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        builder.append('\\');
                        builder.append('u');
                        builder.append(hexDigit[((aChar >> 12) & 0xF)]);
                        builder.append(hexDigit[((aChar >> 8) & 0xF)]);
                        builder.append(hexDigit[((aChar >> 4) & 0xF)]);
                        builder.append(hexDigit[(aChar & 0xF)]);
                    } else {
                        builder.append(aChar);
                    }
            }
        }
    }


    /**
     * 读取字符串，前面有一个字符串长度字节
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @param compressed 压缩标示
     * @return 返回值 字符串
     * @throws Exception 序列化异常
     */
    public static String readString(final ByteBuffer in, final int lengthSize, final boolean compressed) throws Exception {
        int length;
        if (lengthSize == 1) {
            byte[] bytes = new byte[1];
            in.get(bytes);
            length = bytes[0] & 0xff;
        } else if (lengthSize == 2) {
            length = in.getShort();
        } else {
            length = in.getInt();
        }
        return read(in, length, compressed, "UTF-8");
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param length     长度
     * @param compressed 压缩
     * @param charset    字符集
     * @return 返回值 字符串
     * @throws Exception 序列化/反序列化错误
     */
    public static String read(final ByteBuffer in, final int length, final boolean compressed, String charset) throws Exception {
        if (length <= 0) {
            return StringUtils.EMPTY;
        }
        byte[] bytes = readBytes(in, length);
        try {
            if (compressed) {
                bytes = Compressors.decompress(bytes, 0, bytes.length, Zip.INSTANCE);
            }

            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8";
            }
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    /**
     * 读取字节数
     *
     * @param in     输入缓冲区
     * @param length 长度
     */
    public static byte[] readBytes(final ByteBuffer in, final int length) {
        if (in == null || length <= 0) {
            return new byte[0];
        }

        int len = in.remaining();
        if (len == 0) {
            return new byte[0];
        }
        if (length < len) {
            len = length;
        }

        byte[] bytes = new byte[len];
        in.get(bytes);
        return bytes;

    }

    /**
     * 把Properties字符串转换成Map
     *
     * @param text 字符串
     * @return 返回值 散列对象
     * @throws IOException 序列化异常
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> toStringMap(final String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Properties properties = new Properties();
        properties.load(new StringReader(text));

        return new HashMap(properties);
    }

    /**
     * 读取字符串，字符长度&lt;=255
     *
     * @param in 输入缓冲区
     * @return 返回值 字符串
     * @throws Exception 序列化异常
     */
    public static String readString(final ByteBuffer in) throws Exception {
        return readString(in, 1, false);
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @return 返回值 字符串
     * @throws Exception 序列化异常
     */
    public static String readString(final ByteBuffer in, final int lengthSize) throws Exception {
        return readString(in, lengthSize, false);
    }

    /**
     * 获取字节数组
     *
     * @param value   字符串
     * @param charset 字符集
     * @return 返回值 字节数组
     */
    public static byte[] getBytes(final String value, final Charset charset) {
        if (value == null) {
            return new byte[0];
        }

        byte[] bytes;
        if (charset == null) {
            bytes = value.getBytes(Charsets.UTF_8);
        } else {
            bytes = value.getBytes(charset);
        }
        return bytes;
    }


}
