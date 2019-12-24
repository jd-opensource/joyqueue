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
package org.joyqueue.toolkit.security;

import org.joyqueue.toolkit.security.Hex;
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
