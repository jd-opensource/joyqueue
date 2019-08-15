package io.chubao.joyqueue.toolkit.io;

import com.google.common.base.Charsets;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by hexiaofeng on 16-5-6.
 */
public class CompressorTest {

    @Test
    public void testCompress() throws IOException {
        String test = "中国人民122Addf";
        Compressor[] compressors = new Compressor[]{Zip.INSTANCE, Zlib.INSTANCE, Snappy.INSTANCE};
        for (Compressor compressor : compressors) {
            byte[] data = Compressors.compress(test, compressor);
            String result = Compressors.decompress(data, Charsets.UTF_8, compressor);
            Assert.assertEquals(test, result);
        }
    }
}
