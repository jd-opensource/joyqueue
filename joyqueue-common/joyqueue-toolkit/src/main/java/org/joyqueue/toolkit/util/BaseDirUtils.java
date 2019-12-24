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
package org.joyqueue.toolkit.util;


import java.io.File;
import java.io.IOException;

/**
 * @author liyue25
 * Date: 2018/8/29
 */
public class BaseDirUtils {
    public static File prepareBaseDir(String basePath) throws IOException {
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        File tempDirFile = new File(tempDir);
        assert tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite();

        File base = new File(basePath);
        if (base.exists()) {
            if (base.isDirectory()) deleteFolder(base);
            else base.delete();
        }
        base.mkdirs();
        return base;
    }

    public static File prepareBaseDir() throws IOException {
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        return prepareBaseDir(tempDir + File.separator + "joyqueuedata");
    }

    public static void destroyBaseDir() {
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);

        destroyBaseDir(new File(tempDir + File.separator + "joyqueuedata"));
    }

    public static void destroyBaseDir(File base) {
        if (base.exists()) {
            if (base.isDirectory()) deleteFolder(base);
            else base.delete();
        }

    }


    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
