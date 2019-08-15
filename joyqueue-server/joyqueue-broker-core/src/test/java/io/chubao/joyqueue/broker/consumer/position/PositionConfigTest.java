package io.chubao.joyqueue.broker.consumer.position;

import io.chubao.joyqueue.toolkit.util.BaseDirUtils;
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