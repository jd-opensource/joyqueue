package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.bytes.Bytes;

import java.nio.ReadOnlyBufferException;

/**
 * Read-only buffer.
 */
public class ReadOnlyBuffer extends AbstractBuffer {
    protected final Buffer root;

    ReadOnlyBuffer(final Buffer buffer) {
        super(buffer.bytes());
        this.root = buffer;
    }

    @Override
    public boolean isDirect() {
        return root.isDirect();
    }

    @Override
    public boolean isFile() {
        return root.isFile();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Buffer compact() {
        throw new ReadOnlyBufferException();
    }

    @Override
    protected void compact(final long from, final long to, final long length) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void acquire() {
        root.acquire();
    }

    @Override
    public boolean release() {
        return root.release();
    }

    @Override
    public Buffer zero(final long offset, final long length) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer zero(final long offset) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer zero() {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeBoolean(final long offset, final boolean b) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final Buffer buffer) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final Bytes bytes) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final Bytes bytes, final long offset, final long length) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final long offset, final Bytes bytes, final long srcOffset, final long length) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final byte[] bytes) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final byte[] bytes, final long offset, final long length) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer write(final long offset, final byte[] bytes, final long srcOffset, final long length) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeByte(final int b) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeByte(final long offset, final int b) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedByte(final int b) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedByte(final long offset, final int b) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeChar(final char c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeChar(final long offset, final char c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeShort(final short s) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeShort(final long offset, final short s) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedShort(final int s) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedShort(final long offset, final int s) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeMedium(final int m) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeMedium(final long offset, final int m) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedMedium(final int m) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedMedium(final long offset, final int m) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeInt(final int i) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeInt(final long offset, final int i) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedInt(final long i) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUnsignedInt(final long offset, final long i) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeLong(final long l) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeLong(final long offset, final long l) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeFloat(final float f) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeFloat(final long offset, final float f) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeDouble(final double d) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeDouble(final long offset, final double d) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeBoolean(final boolean b) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer writeUTF8(final String s) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public Buffer flush() {
        throw new ReadOnlyBufferException();
    }

    @Override
    public void close() {
        root.close();
    }

}
