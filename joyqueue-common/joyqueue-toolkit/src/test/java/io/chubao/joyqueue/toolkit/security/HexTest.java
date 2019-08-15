package io.chubao.joyqueue.toolkit.security;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class HexTest {

    @Test
    public void testEncode() {
        String source = "12345AB中国6789";
        String encode = Hex.encode(source.getBytes(Charset.forName("utf-8")));
        String decode = new String(Hex.decode(encode),Charset.forName("utf-8"));
        Assert.assertEquals(source, decode);
    }
}
