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
package org.joyqueue.store.cli;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author liyue25
 * Date: 2018/9/20
 */
public class FileTool {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            showUsage();
            System.exit(1);
        }
        String path = args[0];
        long offset = Long.parseLong(args[1]);
        String type = args[2];


        try (RandomAccessFile raf = new RandomAccessFile(new File(path), "r")) {
            raf.seek(offset);
            if ("s".equalsIgnoreCase(type)) {
                System.out.println(raf.readShort());
            } else if ("i".equalsIgnoreCase(type)) {
                System.out.println(raf.readInt());
            } else if ("l".equalsIgnoreCase(type)) {
                System.out.println(raf.readLong());
            } else {
                int length = Integer.parseInt(type);
                byte[] buff = new byte[length];
                raf.read(buff);
                System.out.println(new String(buff));
            }
        }

    }

    private static void showUsage() {
        System.out.println("Usage: FileTool path offset s/i/l/(length of bytes)");
    }
}
