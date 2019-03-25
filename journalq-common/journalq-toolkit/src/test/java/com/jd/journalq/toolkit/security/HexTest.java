package com.jd.journalq.toolkit.security;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class HexTest {

    @Test
    public void testEncode() {
        String source = "12345AB中国6789";
        String encode = Hex.encode(source.getBytes());
        String decode = new String(Hex.decode(encode));
        Assert.assertEquals(source, decode);
    }
}
