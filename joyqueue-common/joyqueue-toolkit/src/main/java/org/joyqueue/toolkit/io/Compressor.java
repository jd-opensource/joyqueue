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
package org.joyqueue.toolkit.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 压缩器
 * Created by hexiaofeng on 16-5-6.
 */
public interface Compressor {

    /**
     * 压缩
     *
     * @param buf    缓冲器
     * @param offset 偏移量
     * @param size   长度
     * @param out    输出流
     * @throws IOException
     */
    void compress(byte[] buf, int offset, int size, OutputStream out) throws IOException;

    /**
     * 解压缩
     *
     * @param buf    缓冲器
     * @param offset 偏移量
     * @param size   长度
     * @param out    输出流
     * @throws java.io.IOException
     */
    void decompress(byte[] buf, int offset, int size, OutputStream out) throws IOException;
}
