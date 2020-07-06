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
package org.joyqueue.store.utils;

import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2018/9/17
 */
public class ByteBufferUtils {

    public static void copy(ByteBuffer from, ByteBuffer to) {
        if (from.remaining() <= to.remaining()) {
            to.put(from);
        } else {
            int toSize = to.remaining();
            if (from.hasArray()) {
                to.put(from.array(), from.arrayOffset() + from.position(), to.remaining());
                from.position(from.position() + toSize);
            } else {
                while (to.hasRemaining() && from.hasRemaining()) {
                    to.put(from.get());
                }
            }

        }
    }


    /**
     * 将srcs中的内容连起来，依次copy到dest中，直到dest满了或者都copy完了。
     */
    public static void concat(ByteBuffer dest, ByteBuffer... srcs) {
        for (ByteBuffer src : srcs) {
            copy(src, dest);
            if (!dest.hasRemaining()) break;
        }

    }
}
