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
package org.joyqueue.toolkit.io.snappy;

import org.joyqueue.toolkit.io.Files;
import com.google.common.base.Preconditions;
import org.joyqueue.toolkit.security.Crc32C;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static java.lang.Math.min;

/**
 * Implements the <a href="http://snappy.googlecode.com/svn/trunk/framing_format.txt" >x-snappy-framed</a> as an
 * {@link InputStream}.
 */
public class SnappyFramedInputStream extends InputStream {
    private final InputStream in;
    private final byte[] frameHeader;
    private final boolean verifyChecksums;
    private final BufferRecycler recycler;

    /**
     * A single frame read from the underlying {@link InputStream}.
     */
    private byte[] input;

    /**
     * The decompressed data from {@link #input}.
     */
    private byte[] uncompressed;

    /**
     * Indicates if this instance has been closed.
     */
    private boolean closed;

    /**
     * Indicates if we have reached the EOF on {@link #in}.
     */
    private boolean eof;

    /**
     * The position in {@link #input} to read to.
     */
    private int valid;

    /**
     * The next position to read from {@link #buffer}.
     */
    private int position;

    /**
     * Buffer is a reference to the real buffer of uncompressed data for the
     * current block: uncompressed if the block is compressed, or input if it is
     * not.
     */
    private byte[] buffer;

    public SnappyFramedInputStream(InputStream in, boolean verifyChecksums) throws IOException {
        this(in, SnappyFramedOutputStream.MAX_BLOCK_SIZE, 4, verifyChecksums, SnappyFramed.HEADER_BYTES);
    }

    /**
     * Creates a Snappy input stream to read data from the specified underlying
     * input stream.
     *
     * @param in              the underlying input stream
     * @param maxBlockSize    max block size
     * @param verifyChecksums if true, checksums in input stream will be verified
     * @param frameHeaderSize frame header size
     * @param expectedHeader  the expected stream header
     */
    public SnappyFramedInputStream(InputStream in, int maxBlockSize, int frameHeaderSize, boolean verifyChecksums,
            byte[] expectedHeader) throws IOException {
        this.in = in;
        this.verifyChecksums = verifyChecksums;
        this.recycler = BufferRecycler.instance();
        this.input = recycler.allocInputBuffer(maxBlockSize + 5);
        this.uncompressed = recycler.allocDecodeBuffer(maxBlockSize + 5);
        this.frameHeader = new byte[frameHeaderSize];

        // stream must begin with stream header
        byte[] actualHeader = new byte[expectedHeader.length];

        int read = Files.readBytes(in, actualHeader, 0, actualHeader.length);
        if (read < expectedHeader.length) {
            throw new EOFException("encountered EOF while reading stream header");
        }
        if (!Arrays.equals(expectedHeader, actualHeader)) {
            throw new IOException("invalid stream header");
        }
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            return -1;
        }
        if (!ensureBuffer()) {
            return -1;
        }
        return buffer[position++] & 0xFF;
    }

    @Override
    public int read(final byte[] output, final int offset, final int length) throws IOException {
        Preconditions.checkNotNull(output, "output is null");
        Preconditions.checkPositionIndexes(offset, offset + length, output.length);
        if (closed) {
            return -1;
        }
        if (length == 0) {
            return 0;
        }
        if (!ensureBuffer()) {
            return -1;
        }

        int size = min(length, available());
        System.arraycopy(buffer, position, output, offset, size);
        position += size;
        return size;
    }

    @Override
    public int available() throws IOException {
        if (closed) {
            return 0;
        }
        return valid - position;
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            if (!closed) {
                closed = true;
                recycler.releaseInputBuffer(input);
                recycler.releaseDecodeBuffer(uncompressed);
            }
        }
    }

    private boolean ensureBuffer() throws IOException {
        if (available() > 0) {
            return true;
        }
        if (eof) {
            return false;
        }

        if (!readBlockHeader()) {
            eof = true;
            return false;
        }

        // get action based on header
        FrameMetaData frameMetaData = getFrameMetaData(frameHeader);

        if (FrameAction.SKIP == frameMetaData.frameAction) {
            Files.skip(in, frameMetaData.length);
            return ensureBuffer();
        }

        if (frameMetaData.length > input.length) {
            this.input = recycler.allocInputBuffer(frameMetaData.length);
            this.uncompressed = recycler.allocDecodeBuffer(frameMetaData.length);
        }

        int actualRead = Files.readBytes(in, input, 0, frameMetaData.length);
        if (actualRead != frameMetaData.length) {
            throw new EOFException("unexpectd EOF when reading frame");
        }

        FrameData frameData = getFrameData(frameHeader, input, actualRead);

        if (FrameAction.UNCOMPRESS == frameMetaData.frameAction) {
            int uncompressedLength = SnappyDecompressor.getUncompressedLength(input, frameData.offset);

            if (uncompressedLength > uncompressed.length) {
                uncompressed = recycler.allocDecodeBuffer(uncompressedLength);
            }

            this.valid = SnappyDecompressor
                    .uncompress(input, frameData.offset, actualRead - frameData.offset, uncompressed, 0);
            this.buffer = uncompressed;
            this.position = 0;
        } else {
            // we need to start reading at the offset
            this.position = frameData.offset;
            this.buffer = input;
            // valid is until the end of the read data, regardless of offset
            // indicating where we start
            this.valid = actualRead;
        }

        if (verifyChecksums) {
            int actualCrc32c = Crc32C.mask(buffer, position, valid - position);
            if (frameData.checkSum != actualCrc32c) {
                throw new IOException("Corrupt input: invalid checksum");
            }
        }

        return true;
    }

    private boolean readBlockHeader() throws IOException {
        int read = Files.readBytes(in, frameHeader, 0, frameHeader.length);

        if (read == -1) {
            return false;
        }

        if (read < frameHeader.length) {
            throw new EOFException("encountered EOF while reading block header");
        }

        return true;
    }

    /**
     * Use the content of the frameHeader to describe what type of frame we have
     * and the action to take.
     */
    protected FrameMetaData getFrameMetaData(final byte[] frameHeader) throws IOException {
        int length = (frameHeader[1] & 0xFF);
        length |= (frameHeader[2] & 0xFF) << 8;
        length |= (frameHeader[3] & 0xFF) << 16;

        int minLength;
        FrameAction frameAction;
        int flag = frameHeader[0] & 0xFF;
        switch (flag) {
            case SnappyFramed.COMPRESSED_DATA_FLAG:
                frameAction = FrameAction.UNCOMPRESS;
                minLength = 5;
                break;
            case SnappyFramed.UNCOMPRESSED_DATA_FLAG:
                frameAction = FrameAction.RAW;
                minLength = 5;
                break;
            case SnappyFramed.STREAM_IDENTIFIER_FLAG:
                if (length != 6) {
                    throw new IOException("stream identifier chunk with invalid length: " + length);
                }
                frameAction = FrameAction.SKIP;
                minLength = 6;
                break;
            default:
                // Reserved unskippable chunks (chunk types 0x02-0x7f)
                if (flag <= 0x7f) {
                    throw new IOException("unsupported unskippable chunk: " + Integer.toHexString(flag));
                }

                // all that is left is Reserved skippable chunks (chunk types 0x80-0xfe)
                frameAction = FrameAction.SKIP;
                minLength = 0;
        }

        if (length < minLength) {
            throw new IOException("invalid length: " + length + " for chunk flag: " + Integer.toHexString(flag));
        }

        return new FrameMetaData(frameAction, length);
    }

    /**
     * Take the frame header and the content of the frame to describe metadata
     * about the content.
     *
     * @param frameHeader The frame header.
     * @param content     The content of the of the frame. Content begins at index {@code 0}.
     * @param length      The length of the content.
     * @return Metadata about the content of the frame.
     */
    protected FrameData getFrameData(final byte[] frameHeader, final byte[] content, final int length) {
        // crc is contained in the frame content
        int crc32c =
                (content[3] & 0xFF) << 24 | (content[2] & 0xFF) << 16 | (content[1] & 0xFF) << 8 | (content[0] & 0xFF);

        return new FrameData(crc32c, 4);
    }

    enum FrameAction {
        RAW,
        SKIP,
        UNCOMPRESS
    }

    /**
     * 片段元数据
     */
    protected static final class FrameMetaData {
        // 长度
        final int length;
        // 操作
        final FrameAction frameAction;

        /**
         * @param frameAction
         * @param length
         */
        public FrameMetaData(FrameAction frameAction, int length) {
            this.frameAction = frameAction;
            this.length = length;
        }
    }

    /**
     * 片段数据
     */
    protected static final class FrameData {
        // 校验和
        final int checkSum;
        // 偏移量
        final int offset;

        public FrameData(int checkSum, int offset) {
            this.checkSum = checkSum;
            this.offset = offset;
        }
    }
}
