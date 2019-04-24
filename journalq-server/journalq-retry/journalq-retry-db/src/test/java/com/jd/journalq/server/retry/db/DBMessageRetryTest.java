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
package com.jd.journalq.server.retry.db;

import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.toolkit.time.SystemClock;
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
    public void addRetry() throws JournalqException {
        List<RetryMessageModel> retryMessageModelList = new ArrayList<>();

        RetryMessageModel retry = new RetryMessageModel();
        retry.setBusinessId("business");
        retry.setTopic("topic");
        retry.setApp("app");
        retry.setPartition((short) 255);
        retry.setIndex(100l);
        retry.setBrokerMessage(new byte[168]);
        retry.setException(new byte[16]);
        retry.setSendTime(SystemClock.now());

        retryMessageModelList.add(retry);

        dbMessageRetry.addRetry(retryMessageModelList);
    }


    @Test
    public void retrySuccess() throws JournalqException {
        String topic = "topic";
        String app = "app";
        Long[] messageIds = {1l};
        dbMessageRetry.retrySuccess(topic, app, messageIds);
    }

    @Test
    public void retryError() throws JournalqException {
        String topic = "topic";
        String app = "app";
        Long[] messageIds = {1l};
        dbMessageRetry.retryError(topic, app, messageIds);
    }

    @Test
    public void retryExpire() throws JournalqException {
        String topic = "topic";
        String app = "app";
        Long[] messageIds = {1l};

        dbMessageRetry.retryExpire(topic, app, messageIds);
    }

    @Test
    public void getRetry() throws JournalqException {
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
    public void countRetry() throws JournalqException {
        String topic = "topic";
        String app = "app";
        int count = dbMessageRetry.countRetry(topic, app);
        System.out.println(count);
    }


}