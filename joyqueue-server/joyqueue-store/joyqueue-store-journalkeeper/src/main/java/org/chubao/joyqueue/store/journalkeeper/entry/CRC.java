package org.chubao.joyqueue.store.journalkeeper.entry;


import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author LiYue
 * Date: 2019/10/14
 */
class CRC {
    static long crc(ByteBuffer buffer) {
        if (buffer.remaining() > 0) {
            CRC32 crc32 = new CRC32();
            crc32.update(buffer);
            return crc32.getValue();
        } else {
            return 0L;
        }
    }
}
