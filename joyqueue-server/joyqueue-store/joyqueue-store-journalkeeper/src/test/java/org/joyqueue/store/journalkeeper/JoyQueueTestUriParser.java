package org.joyqueue.store.journalkeeper;

import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.spi.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author LiYue
 * Date: 2019/9/30
 */
@Singleton
public class JoyQueueTestUriParser implements URIParser {
    private static final Logger logger = LoggerFactory.getLogger(JoyQueueTestUriParser.class);
    private static final String SCHEME = "joyqueue-test";
    private int portOffset = 7;
    @Override
    public String[] supportedSchemes() {
        return new String [] {SCHEME};
    }

    @Override
    public InetSocketAddress parse(URI uri) {
        return new InetSocketAddress("127.0.0.1", 8888);
    }


    public URI create(String topic, int group, int brokerId) {
        try {
            return new URI(SCHEME, String.valueOf(brokerId),  "/" + topic + "/" + group, null);
        } catch (URISyntaxException e) {
            logger.warn("Create uri failed!", e);
            return null;
        }
    }

    public int getBrokerId(URI uri) {
        return Integer.parseInt(uri.getHost());
    }
}
