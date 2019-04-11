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
package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.io.Files;
import com.jd.journalq.toolkit.os.Systems;

import java.io.File;
import java.util.UUID;

public abstract class FileTesting {

    public static final String TEMP_DIR = "export/Data/buffer";

    public static File createFile() {
        File file = Files.path(Systems.getUserHome(), TEMP_DIR, UUID.randomUUID().toString());
        file.getParentFile().mkdirs();
        return file;
    }

    public static void cleanFiles() {
        Files.deleteDirectory(Files.path(Systems.getUserHome(), TEMP_DIR));
    }
}
