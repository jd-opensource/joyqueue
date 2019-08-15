package io.chubao.joyqueue.store.file;

import java.io.File;

/**
 * @author liyue25
 * Date: 2019-05-10
 */
public class DiskFullException extends RuntimeException {
    private File file;
    public DiskFullException(File file) {
        super(String.format("No enough space on disk, create file %s failed!", file.getAbsolutePath()));
        this.file = file;
    }
}
