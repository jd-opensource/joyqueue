/**
 * Copyright 2018 The JoyQueue Authors.
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
