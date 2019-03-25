package com.jd.journalq.broker.consumer.position;

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