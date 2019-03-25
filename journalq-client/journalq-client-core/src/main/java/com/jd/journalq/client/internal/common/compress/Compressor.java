package com.jd.journalq.client.internal.common.compress;

import com.jd.laf.extension.Type;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Compressor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/2
 */
public interface Compressor extends Type<String> {

    void compress(byte[] bytes, int offset, int size, OutputStream out) throws IOException;

    void decompress(byte[] bytes, int offset, int size, OutputStream out) throws IOException;
}