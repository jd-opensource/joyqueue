/**
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
