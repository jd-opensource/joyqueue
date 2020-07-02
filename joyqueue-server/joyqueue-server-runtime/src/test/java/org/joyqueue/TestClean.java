package org.joyqueue;

import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.service.Service;
import org.junit.Test;
import java.io.File;

public class TestClean extends Service {

    private String DEFAULT_JOYQUEUE="joyqueue";
    private String ROOT_DIR =System.getProperty("java.io.tmpdir")+ File.separator+DEFAULT_JOYQUEUE;


    @Test
    public void cleanup() throws Exception{

        Files.deleteDirectory(new File(ROOT_DIR));
    }
}
