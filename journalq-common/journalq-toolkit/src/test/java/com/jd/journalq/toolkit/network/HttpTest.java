package com.jd.journalq.toolkit.network;

import com.jd.journalq.toolkit.URL;
import com.jd.journalq.toolkit.network.http.Get;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class HttpTest {

    @Test
    public void testHttp() throws IOException {
        String html = Get.Builder.build().create().get(URL.valueOf("http://www.baidu.com"));
        Assert.assertTrue(html.contains("baidu"));
    }
}
