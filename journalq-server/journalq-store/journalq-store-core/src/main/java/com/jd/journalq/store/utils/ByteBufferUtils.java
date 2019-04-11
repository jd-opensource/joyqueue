package com.jd.journalq.store.utils;

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
