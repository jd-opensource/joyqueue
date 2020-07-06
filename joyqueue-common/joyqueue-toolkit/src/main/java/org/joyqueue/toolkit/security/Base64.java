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
package org.joyqueue.toolkit.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by hexiaofeng on 16-7-28.
 */
public class Base64 {
    /**
     * 标准BASE64
     */
    public static final int TYPE_STANDARD = 0;
    /**
     * URL安全的BASE64编码
     */
    public static final int TYPE_URL_SAFE = 1;
    /**
     * ORDERED的BASE64编码
     */
    public static final int TYPE_ORDERED = 2;
    /**
     * 多行编码
     */
    public static final int ENCODE_BREAK_LINE = 4;

    private static final byte[] STANDARD_ENCODE_TABLE =
            {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                    'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', '+', '/'};
    private static final byte[] STANDARD_DECODE_TABLE =
            {-3, -3, -3, -3, -3, -3, -3, -3, -3, -2, -2, -3, -3, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 62, -3, -3, -3, 63, 52, 53, 54, 55,
                    56, 57, 58, 59, 60, 61, -3, -3, -3, -1, -3, -3, -3, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                    14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -3, -3, -3, -3, -3, -3, 26, 27, 28, 29, 30, 31, 32,
                    33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3};
    private static final byte[] URL_SAFE_ENCODE_TABLE =
            {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                    'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', '-', '_'};
    private static final byte[] URL_SAFE_DECODE_TABLE =
            {-3, -3, -3, -3, -3, -3, -3, -3, -3, -2, -2, -3, -3, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 45, -3, -3, 48, 49, 50, 51,
                    52, 53, 54, 55, 56, 57, -3, -3, -3, -1, -3, -3, -3, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76,
                    77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, -3, -3, -3, -3, 95, -3, 97, 98, 99, 100,
                    101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
                    121, 122, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3};
    private static final byte[] ORDERED_ENCODE_TABLE =
            {'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', 'a', 'b', 'c',
                    'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
                    'x', 'y', 'z'};
    private static final byte[] ORDERED_DECODE_TABLE =
            {-3, -3, -3, -3, -3, -3, -3, -3, -3, -2, -2, -3, -3, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -2, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 45, -3, -3, 48, 49, 50, 51,
                    52, 53, 54, 55, 56, 57, -3, -3, -3, -1, -3, -3, -3, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76,
                    77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, -3, -3, -3, -3, 95, -3, 97, 98, 99, 100,
                    101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
                    121, 122, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
                    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3};
    private static final byte ENCODE_END = '=';
    private static final byte DECODE_END = -1;
    private static final byte DECODE_SPACE = -2;
    private static final byte DECODE_ERR = -3;
    private static final byte NEW_LINE = '\n';
    private static final int LINE_MAX = 76;
    private static final int BUFF_DECODE_BLOCK = 1024;

    /**
     * 获取编码映射表
     *
     * @param options 模式
     * @return 编码映射表
     */
    private static byte[] getEncodeTable(final int options) {
        switch (options & 0x03) {
            case TYPE_URL_SAFE:
                return URL_SAFE_ENCODE_TABLE;
            case TYPE_ORDERED:
                return ORDERED_ENCODE_TABLE;
            default:
                return STANDARD_ENCODE_TABLE;
        }
    }

    /**
     * 获取解码映射表
     *
     * @param options 模式
     * @return 解码映射表
     */
    private static byte[] getDecodeTable(final int options) {
        switch (options & 0x03) {
            case TYPE_URL_SAFE:
                return URL_SAFE_DECODE_TABLE;
            case TYPE_ORDERED:
                return ORDERED_DECODE_TABLE;
            default:
                return STANDARD_DECODE_TABLE;
        }
    }

    /**
     * 3字节到4字节转换函数
     *
     * @param table 编码映射表
     * @param data  数据
     * @param index 起始索引
     * @param len   数据长度
     * @param buff  缓存数组
     * @param pos   写入缓存起始索引
     */
    private static void byte3to4(final byte[] table, final byte[] data, final int index, final int len,
            final byte[] buff, final int pos) {
        switch (len) {
            case 1:
                buff[pos] = table[data[index] >> 2];
                buff[pos + 1] = table[(data[index] & 0x03) << 4];
                buff[pos + 2] = ENCODE_END;
                buff[pos + 3] = ENCODE_END;
                break;
            case 2:
                buff[pos] = table[data[index] >> 2];
                buff[pos + 1] = table[((data[index] & 0x03) << 4) | (data[index + 1] >> 4)];
                buff[pos + 2] = table[(data[index + 1] & 0x0F) << 2];
                buff[pos + 3] = ENCODE_END;
                break;
            default:
                buff[pos] = table[data[index] >> 2];
                buff[pos + 1] = table[((data[index] & 0x03) << 4) | (data[index + 1] >> 4)];
                buff[pos + 2] = table[((data[index + 1] & 0x0F) << 2) | (data[index + 2] >> 6)];
                buff[pos + 3] = table[data[index + 2] & 0x3F];
        }
    }

    /**
     * 4字节到3字节转换函数
     *
     * @param table 解码映射表
     * @param data  数据
     * @param len   数据长度
     * @param buff  缓存数组
     * @param pos   写入缓存起始索引
     */
    private static void byte4to3(final byte[] table, final byte[] data, final int len, final byte[] buff,
            final int pos) {
        switch (len) {
            case 1:
                //error
                break;
            case 2:
                buff[pos] = (byte) ((table[data[0]] << 2) | (table[data[1]] >> 4));
                break;
            case 3:
                buff[pos] = (byte) ((table[data[0]] << 2) | (table[data[1]] >> 4));
                buff[pos + 1] = (byte) (((table[data[1]] & 0x0F) << 4) | (table[data[2]] >> 2));
                break;
            default:
                buff[pos] = (byte) ((table[data[0]] << 2) | (table[data[1]] >> 4));
                buff[pos + 1] = (byte) (((table[data[1]] & 0x0F) << 4) | (table[data[2]] >> 2));
                buff[pos + 2] = (byte) (((table[data[2]] & 0x03) << 6) | table[data[3]]);
        }
    }

    /**
     * BASE64编码数据
     *
     * @param data 数据
     * @return BASE64字符串
     */
    public static String encode(final byte[] data) {
        return encode(data, TYPE_STANDARD);
    }

    /**
     * BASE64编码数据
     *
     * @param data    数据
     * @param options 编码参数
     * @return BASE64字符串
     */
    public static String encode(final byte[] data, final int options) {
        byte[] encodeMap = getEncodeTable(options);
        boolean breakline = (options & ENCODE_BREAK_LINE) != 0;
        int len = (data.length + 2) / 3;
        int lineSize = LINE_MAX / 4;
        int lenAdd = breakline ? (len / lineSize) : 0;
        byte[] buff = new byte[len * 4 + lenAdd];
        int blCnt = 0;
        for (int i = 0; i < len; i++) {
            if (breakline && i > 0 && i % lineSize == 0) {
                buff[i * 4 + blCnt++] = NEW_LINE;
            }
            byte3to4(encodeMap, data, i * 3, data.length - i * 3, buff, i * 4 + blCnt);
        }
        return new String(buff);
    }

    /**
     * 使用输入输出流编码
     *
     * @param in  输入流
     * @param out 输出流
     * @throws IOException
     */
    public static void encode(final InputStream in, final OutputStream out) throws IOException {
        encode(in, out, TYPE_STANDARD);
    }

    /**
     * 使用输入输出流编码
     *
     * @param in      输入流
     * @param out     输出流
     * @param options 编码参数，使用或操作并BASE64.[X]
     * @throws IOException
     */
    public static void encode(final InputStream in, final OutputStream out, final int options) throws IOException {
        byte[] encodeMap = getEncodeTable(options);
        boolean breakLine = (options & ENCODE_BREAK_LINE) != 0;
        int lineSize = LINE_MAX / 4;
        byte[] buff = new byte[4];
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        byte[] data = new byte[3];
        int readCnt;
        int solveCnt = 0;
        while ((readCnt = bin.read(data)) != -1) {
            if (readCnt > 0) {
                if (breakLine && solveCnt > 0 && solveCnt % lineSize == 0) {
                    bout.write(NEW_LINE);
                }
                byte3to4(encodeMap, data, 0, readCnt, buff, 0);
                bout.write(buff);
                solveCnt++;
            }
        }
        bout.flush();
    }

    /**
     * BASE64解码数据
     *
     * @param str BASE64字符串
     * @return 数据，错误返回null
     */
    public static byte[] decode(final String str) {
        return decode(str, TYPE_STANDARD);
    }

    /**
     * BASE64解码数据
     *
     * @param str     BASE64字符串
     * @param options 解码参数
     * @return 数据，错误返回null
     */
    public static byte[] decode(final String str, final int options) {
        byte[] table = getDecodeTable(options);
        byte[] data = new byte[4];
        byte[] buff = new byte[3 * BUFF_DECODE_BLOCK];
        ArrayList<byte[]> buffs = new ArrayList<byte[]>();
        int dataCnt = 0;
        int buffCnt = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch > 255) {
                return null;
            }
            byte b = (byte) ch;
            byte db = table[b];
            if (db == DECODE_ERR) {
                return null;//error char
            } else if (db == DECODE_SPACE) {
                continue;//skip space
            } else if (db == DECODE_END) {
                continue;//skip end char
            } else {
                if (dataCnt == 4) {
                    byte4to3(table, data, dataCnt, buff, buffCnt);
                    dataCnt = 0;
                    buffCnt += 3;
                    if (buffCnt >= buff.length) {
                        buffs.add(buff);
                        buffCnt = 0;
                    }
                }
                data[dataCnt++] = b;
            }
        }
        if (dataCnt > 0) {
            byte4to3(table, data, dataCnt, buff, buffCnt);
            buffCnt += dataCnt - 1;
        }
        int totalLen = buffCnt;
        for (byte[] bs : buffs) {
            totalLen += bs.length;
        }
        byte[] result = new byte[totalLen];
        for (int i = 0; i < buffs.size(); i++) {
            System.arraycopy(buffs.get(i), 0, result, i * 3 * BUFF_DECODE_BLOCK, 3 * BUFF_DECODE_BLOCK);
        }
        if (buffCnt > 0) {
            System.arraycopy(buff, 0, result, buffs.size() * 3 * BUFF_DECODE_BLOCK, buffCnt);
        }
        return result;
    }

    /**
     * 使用输入输出流解码
     *
     * @param in  输入流
     * @param out 输出流
     * @throws IOException
     */
    public static void decode(final InputStream in, final OutputStream out) throws IOException {
        decode(in, out, TYPE_STANDARD);
    }

    /**
     * 使用输入输出流解码
     *
     * @param in      输入流
     * @param out     输出流
     * @param options 解码参数
     * @throws IOException
     */
    public static void decode(final InputStream in, final OutputStream out, final int options) throws IOException {
        byte[] table = getDecodeTable(options);
        byte[] data = new byte[4];
        byte[] buff = new byte[3 * BUFF_DECODE_BLOCK];
        int dataCnt = 0;
        int buffCnt = 0;
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        int read;
        while ((read = bin.read()) != -1) {
            char ch = (char) read;
            if (ch > 255) {
                throw new IOException("unsupported char " + ch);
            }
            byte b = (byte) ch;
            byte db = table[b];
            if (db == DECODE_ERR) {
                throw new IOException("unsupported char " + ch);
            } else if (db == DECODE_SPACE) {
                continue;//skip space
            } else if (db == DECODE_END) {
                continue;//skip end char
            } else {
                if (dataCnt == 4) {
                    byte4to3(table, data, dataCnt, buff, buffCnt);
                    dataCnt = 0;
                    buffCnt += 3;
                    if (buffCnt >= buff.length) {
                        bout.write(buff, 0, buffCnt);
                        buffCnt = 0;
                    }
                }
                data[dataCnt++] = b;
            }
        }
        if (dataCnt > 0) {
            byte4to3(table, data, dataCnt, buff, buffCnt);
            buffCnt += dataCnt - 1;
        }
        if (buffCnt > 0) {
            bout.write(buff, 0, buffCnt);
        }
        bout.flush();

    }
}