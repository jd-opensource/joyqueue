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
package org.joyqueue.broker.consumer.position;

import org.joyqueue.broker.consumer.position.PositionConfig;
import org.joyqueue.toolkit.util.BaseDirUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class PositionConfigTest {
    private final static String PATH = "position_store";
    private File base;
    private PositionConfig config ;


    @Before
    public void before() throws IOException {
        base = BaseDirUtils.prepareBaseDir(PATH);
        config = new PositionConfig(base.getCanonicalPath());
    }

    @Test
    public void getPositionFile() {
        File positionFile = config.getPositionFile();
        String absolutePath = positionFile.getPath();
        Assert.assertEquals(new File(base, "index").getAbsolutePath(), absolutePath);
    }

    @After
    public void after() {
        BaseDirUtils.destroyBaseDir(base);
    }
}