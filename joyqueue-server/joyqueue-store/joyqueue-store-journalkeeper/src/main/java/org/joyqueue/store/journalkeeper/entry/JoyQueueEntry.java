package org.joyqueue.store.journalkeeper.entry;

import io.journalkeeper.core.api.BytesFragment;
import io.journalkeeper.core.api.JournalEntry;
import io.journalkeeper.core.journal.ParseJournalException;
import org.joyqueue.store.message.BatchMessageParser;
import org.joyqueue.store.message.MessageParser;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author LiYue
 * Date: 2019/10/14
 */
class JoyQueueEntry implements JournalEntry {
    private int offset = 0;
    private final byte [] serializedBytes;
    private final ByteBuffer serializedBuffer;

    JoyQueueEntry(byte [] serializedBytes, boolean checkCRC, boolean checkLength) {
        this.serializedBytes = serializedBytes;
        this.serializedBuffer = ByteBuffer.wrap(serializedBytes);

        if(checkCRC&&shouldCheckCRC()) {
            checkCRC(serializedBuffer);
        }

        if(checkLength) {
            checkLength(serializedBytes);
        }

        checkBatchSize();
    }

    /**
     * Ignore crc check for internal partition
     **/
    public boolean shouldCheckCRC(){
        if(getPartition()==Short.MAX_VALUE){
            return false;
        }
        return true;
    }

    private void checkBatchSize() {
        int batchSize;
        if((batchSize = getBatchSize()) < 1) {
            throw new ParseJournalException("Batch size " + batchSize + " should be not less than 1!");
        }
    }

    private void checkCRC(ByteBuffer buffer) {
        ByteBuffer body = MessageParser.getByteBuffer(buffer, MessageParser.BODY);

        if(MessageParser.getLong(buffer, MessageParser.CRC) == CRC.crc(body)) {
            return;
        }
        throw new ParseJournalException("CRC not match!");
    }

    private void checkLength(byte[] serializedBytes) {
        if (serializedBytes.length != getLength()) {
            throw new ParseJournalException(
                    String.format("Declared length %d not equals actual length %d！",
                            getLength(), serializedBytes.length));
        }
    }

    @Override
    public int getBatchSize() {
        return BatchMessageParser.isBatch(serializedBuffer) ?
                BatchMessageParser.getBatchSize(serializedBuffer) :
                1;
    }

    @Override
    public void setBatchSize(int batchSize) {
        BatchMessageParser.setBatch(serializedBuffer, batchSize > 1);
        if(batchSize > 1) {
            BatchMessageParser.setBatchSize(serializedBuffer, (short) batchSize);
        }
    }

    @Override
    public int getPartition() {
        return MessageParser.getShort(serializedBuffer, MessageParser.PARTITION);
    }

    @Override
    public void setPartition(int partition) {
        MessageParser.setShort(serializedBuffer, MessageParser.PARTITION, (short) partition);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getTerm() {
        return MessageParser.getInt(serializedBuffer, MessageParser.TERM);
    }

    @Override
    public void setTerm(int term) {
        MessageParser.setInt(serializedBuffer, MessageParser.TERM, term);
    }

    @Override
    public BytesFragment getPayload() {
        return new BytesFragment(
                serializedBytes,
                MessageParser.getFixedAttributesLength(),
                serializedBytes.length - MessageParser.getFixedAttributesLength());
    }

    @Override
    public byte[] getSerializedBytes() {
        return serializedBytes;
    }

    @Override
    public int getLength() {
        return MessageParser.getInt(serializedBuffer, MessageParser.LENGTH);
    }

    @Override
    public long getTimestamp() {
        return MessageParser.getLong(serializedBuffer, MessageParser.STORAGE_TIMESTAMP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoyQueueEntry that = (JoyQueueEntry) o;
        return Arrays.equals(serializedBytes, that.serializedBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(serializedBytes);
    }
}
