package org.joyqueue.store.journalkeeper.entry;

import io.journalkeeper.core.api.JournalEntry;
import io.journalkeeper.core.api.JournalEntryParser;
import io.journalkeeper.core.entry.DefaultJournalEntry;
import org.joyqueue.store.message.BatchMessageParser;
import org.joyqueue.store.message.MessageParser;

import java.nio.ByteBuffer;

/**
 * @author LiYue
 * Date: 2019/10/14
 */
public class JoyQueueEntryParser implements JournalEntryParser {
    @Override
    public int headerLength() {
        return MessageParser.getFixedAttributesLength();
    }

    @Override
    public JournalEntry parse(byte[] bytes) {
        return new JoyQueueEntry(bytes, true, true);
    }

    @Override
    public JournalEntry parseHeader(byte[] headerBytes) {
        return new JoyQueueEntry(headerBytes, false, false);
    }

    /**
     * Use to communicate with journalkeeper internal
     *
     **/
    @Override
    public JournalEntry createJournalEntry(byte[] payload) {
        int headerLength = headerLength();

        byte [] rawEntry = new byte[headerLength + payload.length];
        System.arraycopy(payload, 0, rawEntry, headerLength, payload.length);
        ByteBuffer buffer = ByteBuffer.wrap(rawEntry);
        MessageParser.setShort(buffer, MessageParser.MAGIC, DefaultJournalEntry.MAGIC_CODE);
        MessageParser.setInt(buffer, MessageParser.LENGTH, rawEntry.length);
        //ByteBuffer body = MessageParser.getByteBuffer(buffer, MessageParser.BODY);
        //MessageParser.setLong(buffer, MessageParser.CRC, CRC.crc(body));
        BatchMessageParser.setBatch(buffer, false);
        return new JoyQueueEntry(rawEntry,false,false);
    }


}
