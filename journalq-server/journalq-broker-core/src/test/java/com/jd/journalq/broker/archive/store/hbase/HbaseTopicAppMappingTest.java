package com.jd.journalq.broker.archive.store.hbase;

import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.hbase.HBaseClient;
import com.jd.journalq.server.archive.store.HBaseTopicAppMapping;
import org.junit.Test;

/**
 * Created by chengzhiliang on 2018/12/14.
 */
public class HbaseTopicAppMappingTest {


    HBaseTopicAppMapping topicAppMapping = new HBaseTopicAppMapping(new HBaseClient("cbase-client-dev.xml"));

    @Test
    public void getTopicId() throws JMQException {
        int topicId = topicAppMapping.getTopicId("test_topic2");
        System.out.println(topicId);
    }

    @Test
    public void getTopicName() throws JMQException {
        getTopicId();
        String topicName = topicAppMapping.getTopicName(1);
        System.out.println(topicName);
    }

    @Test
    public void getAppId() throws JMQException {
        int test_app = topicAppMapping.getAppId("test_app2");
        System.out.println(test_app);
    }

    @Test
    public void getAppName() throws JMQException {
        getAppId();

        String app = topicAppMapping.getAppName(1);
        System.out.println(app);
    }
}