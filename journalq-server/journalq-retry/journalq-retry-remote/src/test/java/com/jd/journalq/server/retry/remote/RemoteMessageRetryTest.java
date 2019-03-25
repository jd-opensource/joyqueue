package com.jd.journalq.server.retry.remote;

import com.google.common.collect.Sets;
import com.jd.journalq.network.transport.TransportClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * Created by chengzhiliang on 2019/2/18.
 */
public class RemoteMessageRetryTest {

    RemoteMessageRetry remoteMessageRetry;


    @Before
    public void init() {
        remoteMessageRetry = new RemoteMessageRetry(new RemoteRetryProvider(){
            @Override
            public Set<String> getUrls() {
                return Sets.newHashSet("10.0.7.18:50089");
            }

            @Override
            public TransportClient createTransportClient() {
//                return new BrokerTransportClientFactory().create(new ClientConfig());;
                return null;
            }
        });

        remoteMessageRetry.start();
    }

    @Test
    public void addRetry() {

    }

}