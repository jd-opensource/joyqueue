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
package com.jd.joyqueue.broker.consumer.position;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class PositionConfigTest {
    private String path = "temp/position_store";
    private PositionConfig config = new PositionConfig(path);

    @Test
    public void getPositionFile() {
        File positionFile = config.getPositionFile();
        String absolutePath = positionFile.getPath();
        Assert.assertEquals(path + "/index", absolutePath);
    }
}