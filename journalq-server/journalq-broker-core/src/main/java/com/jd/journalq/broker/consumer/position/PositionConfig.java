package com.jd.journalq.broker.consumer.position;

import java.io.File;

/**
 * 消费位置配置
 * <p>
 * Created by chengzhiliang on 2018/8/22.
 */
public class PositionConfig {
    //备份文件后缀
    public static String BACK_SUFFIX = ".1";
    //消费位置文件
    private File positionFile;

    public PositionConfig(File dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("dataDirectory can not be null");
        }
        if (dataDirectory.exists()) {
            if (!dataDirectory.isDirectory()) {
                throw new IllegalArgumentException(String.format("%s is not a directory", dataDirectory.getPath()));
            }
        } else {
            if (!dataDirectory.mkdirs()) {
                if (!dataDirectory.exists()) {
                    throw new IllegalArgumentException(
                            String.format("create directory %s error.", dataDirectory.getPath()));
                }
            }
        }
        if (!dataDirectory.canWrite()) {
            throw new IllegalArgumentException(String.format("%s can not be written", dataDirectory.getPath()));
        }
        if (!dataDirectory.canRead()) {
            throw new IllegalArgumentException(String.format("%s can not be read", dataDirectory.getPath()));
        }
        this.positionFile = new File(dataDirectory, "index");
    }

    public PositionConfig(String file) {
        this(new File(file));
    }

    /**
     * 获取消费位置文件
     *
     * @return 消费位置文件
     */
    public File getPositionFile() {
        return positionFile;
    }

}
