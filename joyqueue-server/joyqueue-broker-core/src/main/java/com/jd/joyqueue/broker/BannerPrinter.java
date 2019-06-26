package com.jd.joyqueue.broker;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * BannerPrinter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/26
 */
public class BannerPrinter {

    protected static final Logger logger = LoggerFactory.getLogger(BannerPrinter.class);

    protected static final String BANNER_RESOURCE = "joyqueue/banner";
    protected static final String VERSION_RESOURCE = "joyqueue/version.properties";
    protected static final String PLACEHOLDER_PREFIX = "{";
    protected static final String PLACEHOLDER_SUFFIX = "}";

    public static void print() {
        InputStream inputStream = BannerPrinter.class.getClassLoader().getResourceAsStream(BANNER_RESOURCE);
        try {
            String banner = IOUtils.toString(inputStream);
            Properties params = buildParams();
            banner = render(banner, params);
            print(System.out, banner);
        } catch (IOException e) {
            logger.warn("print banner exception", e);
        }
    }

    protected static Properties buildParams() {
        InputStream versionStream = BannerPrinter.class.getClassLoader().getResourceAsStream(VERSION_RESOURCE);
        Properties params = new Properties();

        try {
            if (versionStream != null) {
                params.load(versionStream);
            }
        } catch (IOException e) {
            logger.warn("load version exception", e);
        }
        return params;
    }

    protected static String render(String banner, Properties params) {
        for (String key : params.stringPropertyNames()) {
            banner = banner.replace(PLACEHOLDER_PREFIX + key + PLACEHOLDER_SUFFIX, params.getProperty(key));
        }
        return banner;
    }

    protected static void print(PrintStream stream, String banner) throws IOException {
        stream.write(banner.getBytes());
    }
}