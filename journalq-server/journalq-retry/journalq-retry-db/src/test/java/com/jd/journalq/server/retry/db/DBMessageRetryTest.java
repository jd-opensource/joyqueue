package com.jd.journalq.server.retry.db;

import com.jd.journalq.exception.JMQException;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/2/15.
 */
public class DBMessageRetryTest {

    private final DBMessageRetry dbMessageRetry = new DBMessageRetry();

    @Before
    public void init() {
        dbMessageRetry.start();
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

        dbMessageRetry.addRetry(retryMessageModelList);
    }


    @Test
    public void retrySuccess() throws JMQException {
        String topic = "topic";
        String app = "app";
        Long[] messageIds = {1l};
        dbMessageRetry.retrySuccess(topic, app, messageIds);
    }

    @Test
    public void retryError() throws JMQException {
        String topic = "topic";
        String app = "app";
        Long[] messageIds = {1l};
        dbMessageRetry.retryError(topic, app, messageIds);
    }

    @Test
    public void retryExpire() throws JMQException {
        String topic = "topic";
        String app = "app";
        Long[] messageIds = {1l};

        dbMessageRetry.retryExpire(topic, app, messageIds);
    }

    @Test
    public void getRetry() throws JMQException {
        String topic = "topic";
        String app = "app";
        short count = 10;
        long startIndex = 0;
        List<RetryMessageModel> retry = dbMessageRetry.getRetry(topic, app, count, startIndex);
        for (RetryMessageModel model : retry) {
            System.out.println(ToStringBuilder.reflectionToString(model));
        }
    }

    @Test
    public void countRetry() throws JMQException {
        String topic = "topic";
        String app = "app";
        int count = dbMessageRetry.countRetry(topic, app);
        System.out.println(count);
    }


}