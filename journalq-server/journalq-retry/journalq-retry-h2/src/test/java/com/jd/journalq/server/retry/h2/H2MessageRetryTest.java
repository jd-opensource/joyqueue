/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.server.retry.h2;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.server.retry.api.RetryPolicyProvider;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chengzhiliang on 2019/3/11.
 */
public class H2MessageRetryTest {
    private final H2MessageRetry dbMessageRetry = new H2MessageRetry();

    @Before
    public void init() {
        dbMessageRetry.start();

        dbMessageRetry.setRetryPolicyProvider(new RetryPolicyProvider() {
            @Override
            public RetryPolicy getPolicy(TopicName topic, String app) throws JMQException {
                return new RetryPolicy();
            }
        });
    }

    private String initSql = "CREATE TABLE message_retry (\n" +
            "\tid bigint(20) NOT NULL AUTO_INCREMENT,\n" +
            "\tmessage_id varchar(50) NOT NULL,\n" +
            "\tbusiness_id varchar(100) DEFAULT NULL,\n" +
            "\ttopic varchar(100) NOT NULL,\n" +
            "\tapp varchar(100) NOT NULL,\n" +
            "\tsend_time datetime NOT NULL,\n" +
            "\texpire_time datetime NOT NULL,\n" +
            "\tretry_time datetime NOT NULL,\n" +
            "\tretry_count int(10) NOT NULL DEFAULT '0',\n" +
            "\tdata mediumblob NOT NULL,\n" +
            "\texception blob,\n" +
            "\tcreate_time datetime NOT NULL,\n" +
            "\tcreate_by int(10) NOT NULL DEFAULT '0',\n" +
            "\tupdate_time datetime NOT NULL,\n" +
            "\tupdate_by int(10) NOT NULL DEFAULT '0',\n" +
            "\tstatus tinyint(4) NOT NULL DEFAULT '1',\n" +
            "\tPRIMARY KEY (id)\n" +
            // "\tKEY `idx_topic_app` (`topic`, `app`, `status`, `retry_time`)\n" +
            ") AUTO_INCREMENT = 0";

    @Test
    public void createDB() throws SQLException {
        DataSource dataSource = dbMessageRetry.getDataSource();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(initSql);
        }
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