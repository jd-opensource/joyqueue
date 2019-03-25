package com.jd.journalq.broker;

import com.google.common.collect.Sets;
import com.jd.journalq.broker.network.support.BrokerTransportClientFactory;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.config.ClientConfig;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.server.retry.remote.RemoteMessageRetry;
import com.jd.journalq.server.retry.remote.RemoteRetryProvider;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chengzhiliang on 2019/2/18.
 */
public class RemoteMessageRetryTest {

    RemoteMessageRetry remoteMessageRetry;


    @Before
    public void init() {
        remoteMessageRetry = new RemoteMessageRetry(new RemoteRetryProvider() {
            @Override
            public Set<String> getUrls() {
                return Sets.newHashSet("10.0.7.18:50089");
            }

            @Override
            public TransportClient createTransportClient() {
                return new BrokerTransportClientFactory().create(new ClientConfig());
            }
        });

        remoteMessageRetry.start();
    }

    @Test
    public void addRetry() throws JMQException {
        List<RetryMessageModel> retryMessageModelList = new ArrayList<>();

        RetryMessageModel retry = new RetryMessageModel();
        retry.setBusinessId("business");
        retry.setTopic("topic");
        retry.setApp("app");
        retry.setPartition((short) 255);
        retry.setIndex(100l);
        retry.setBrokerMessage(new byte[168]);
        retry.setException(new byte[16]);
        retry.setSendTime(System.currentTimeMillis());

        retryMessageModelList.add(retry);

        remoteMessageRetry.addRetry(retryMessageModelList);
    }

    @Test
    public void getRetry() throws JMQException {
        String topic = "topic";
        String app = "app";
        short count = 10;
        long startIndex = 0;
        List<RetryMessageModel> retry = remoteMessageRetry.getRetry(topic, app, count, startIndex);
        for (RetryMessageModel model : retry) {
            System.out.println(ToStringBuilder.reflectionToString(model));
        }
    }

    @Test
    public void countRetry() throws JMQException {
        remoteMessageRetry.countRetry("topic", "app");
    }

    @Test
    public void retrySuccess() throws JMQException {
        remoteMessageRetry.retrySuccess("topic", "app", new Long[]{1l});
    }

    @Test
    public void retryError() throws JMQException {
        remoteMessageRetry.retryError("topic", "app", new Long[]{1l});
    }

    @Test
    public void retryExpire() throws JMQException {
        remoteMessageRetry.retryExpire("topic", "app", new Long[]{1l});
    }

}