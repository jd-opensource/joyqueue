package org.joyqueue.store.utils;

import java.io.File;

/**
 * FileUtils
 * author: gaohaoxiang
 * date: 2020/10/28
 */
public class FileUtils {

    public static boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if (!f.delete()) {
                    }
                }
            }
        }
        return folder.delete();
    }
}