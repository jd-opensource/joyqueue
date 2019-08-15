package io.chubao.joyqueue.client.internal;

import io.chubao.joyqueue.client.internal.transport.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * ClientConsts
 *
 * author: gaohaoxiang
 * date: 2019/3/5
 */
public class ClientConsts {

    protected static final Logger logger = LoggerFactory.getLogger(ClientConsts.class);

    private static final String VERSION_FILE = "META-INF/joyqueue/version.properties";

    public static final String PACKAGE_VERSION = Client.class.getPackage().getImplementationVersion();

    public static final String VERSION;

    static {
        InputStream versionInputStream = ClientConsts.class.getClassLoader().getResourceAsStream(VERSION_FILE);
        String version = PACKAGE_VERSION;

        try {
            Properties properties = new Properties();
            properties.load(versionInputStream);
            version = properties.getProperty("version", PACKAGE_VERSION);
        } catch (Exception e) {
            logger.warn("load version properties error", e);
        } finally {
            try {
                versionInputStream.close();
            } catch (Exception e) {
                logger.warn("close version properties error", e);
            }
        }

        VERSION = version;
    }
}