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
package org.joyqueue.toolkit.os;

import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.os.Systems;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by hexiaofeng on 16-7-4.
 */
public class SystemsTest {

    @Test
    public void testGetCores() throws IOException {
        System.out.println(Systems.getCores());
        Systems.JDOS1CoresDetector.ETC_CONFIG_INFO = "";
        File file = File.createTempFile("config_info", "");
        Files.write(file,
                "{\"Config\": {\"Cpuset\": \"1,2\", \"Memory\": 4294967296}, \"host_ip\": \"10.8.65" + ".251\"}");
        String config = Systems.JDOS1CoresDetector.ETC_CONFIG_INFO;
        try {
            Systems.JDOS1CoresDetector.ETC_CONFIG_INFO = file.getPath();
            int cores = Systems.getCores();
            Assert.assertEquals(cores, 2);
        } finally {
            Systems.JDOS1CoresDetector.ETC_CONFIG_INFO = config;
        }
    }
}
