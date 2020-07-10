package org.joyqueue.store.journalkeeper;

import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.spi.Singleton;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.domain.Broker;
import org.joyqueue.helper.PortHelper;
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
public class JoyQueueUriParser implements URIParser, BrokerContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JoyQueueUriParser.class);
    private BrokerContext brokerContext;
    private static final String SCHEME = "joyqueue";
    @Override
    public String[] supportedSchemes() {
        return new String [] {SCHEME};
    }

    @Override
    public InetSocketAddress parse(URI uri) {
        if(null != brokerContext) {
            Broker broker = brokerContext.getClusterManager().getBrokerById(Integer.parseInt(uri.getAuthority()));
            return new InetSocketAddress(broker.getIp(), PortHelper.getStorePortOffset(broker.getPort()));
        }
        return null;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    public URI create(String topic, int group, int brokerId) {
        try {
            return new URI(SCHEME, String.valueOf(brokerId),  "/" + topic + "/" + group, null);
        } catch (URISyntaxException e) {
            logger.warn("Create uri failed!", e);
            return null;
        }
    }


    /**
     * Parser broker id from uri
     **/
    public int getBrokerId(URI uri) {
        return Integer.parseInt(uri.getHost());
    }

    /**
     * Parser Topic from uri
     **/
    public String getTopic(URI uri) {
        return uri.getPath().split("/")[1];
    }

    /**
     * Parser group from uri
     **/
    public int getGroup(URI uri) {
        return Integer.valueOf(uri.getPath().split("/")[2]);
    }


}
