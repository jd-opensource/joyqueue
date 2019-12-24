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

/**
 * Created by hexiaofeng on 16-5-9.
 */
public abstract class Hex {

    private static final char HEX[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 把字节数组用十六进制表示出来
     *
     * @param data 字节数组
     * @return 十六进制字符串
     */
    public static final String encode(final byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        // 把密文转换成十六进制的字符串形式
        char str[] = new char[data.length * 2];
        int k = 0;
        for (byte b : data) {
            str[k++] = HEX[b >>> 4 & 0xf];
            str[k++] = HEX[b & 0xf];
        }
        return new String(str);
    }

    /**
     * 把字节数组用十六进制表示出来
     *
     * @param data 字节数组
     * @return 十六进制字符串
     */
    public static final byte[] decode(final String data) {
        if (data == null) {
            return null;
        } else if (data.isEmpty()) {
            return new byte[0];
        }
        int length = data.length();
        if ((length % 2) != 0) {
            throw new IllegalArgumentException("length must be a power of 2");
        }
        int size = length >> 1;
        byte[] result = new byte[size];
        char[] chars = data.toCharArray();
        int i = 0;
        int pos;
        byte value;
        for (char ch : chars) {
            switch (ch) {
                case '0':
                    value = 0;
                    break;
                case '1':
                    value = 1;
                    break;
                case '2':
                    value = 2;
                    break;
                case '3':
                    value = 3;
                    break;
                case '4':
                    value = 4;
                    break;
                case '5':
                    value = 5;
                    break;
                case '6':
                    value = 6;
                    break;
                case '7':
                    value = 7;
                    break;
                case '8':
                    value = 8;
                    break;
                case '9':
                    value = 9;
                    break;
                case 'a':
                case 'A':
                    value = 10;
                    break;
                case 'b':
                case 'B':
                    value = 11;
                    break;
                case 'c':
                case 'C':
                    value = 12;
                    break;
                case 'd':
                case 'D':
                    value = 13;
                    break;
                case 'e':
                case 'E':
                    value = 14;
                    break;
                case 'f':
                case 'F':
                    value = 15;
                    break;
                default:
                    throw new IllegalArgumentException("invalid hex string");

            }
            pos = i >> 1;
            if ((i & 0x1) == 0) {
                result[pos] = value;
            } else {
                result[pos] = (byte) (result[pos] << 4 | value);
            }
            i++;
        }
        return result;
    }
}
