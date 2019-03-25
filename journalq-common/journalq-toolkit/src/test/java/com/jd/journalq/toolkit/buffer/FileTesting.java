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
