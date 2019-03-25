package com.jd.journalq.toolkit.os;

import com.jd.journalq.toolkit.io.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by hexiaofeng on 16-7-4.
 */
public class SystemsTest {

    @Test
    public void testGetCores() throws IOException {
        System.out.println(Systems.getCores());
        Systems.JDOS1CoresDetector.ETC_CONFIG_INFO = "";
        File file = File.createTempFile("config_info", "");
        Files.write(file,
                "{\"Config\": {\"Cpuset\": \"1,2\", \"Memory\": 4294967296}, \"host_ip\": \"10.8.65" + ".251\"}");
        String config = Systems.JDOS1CoresDetector.ETC_CONFIG_INFO;
        try {
            Systems.JDOS1CoresDetector.ETC_CONFIG_INFO = file.getPath();
            int cores = Systems.getCores();
            Assert.assertEquals(cores, 2);
        } finally {
            Systems.JDOS1CoresDetector.ETC_CONFIG_INFO = config;
        }
    }
}
