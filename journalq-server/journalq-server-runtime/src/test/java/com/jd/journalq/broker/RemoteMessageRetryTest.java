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
package com.jd.journalq.broker;

/**
 * Created by chengzhiliang on 2019/2/18.
 */
//FIXME: 单元测试未通过
public class RemoteMessageRetryTest {

//    RemoteMessageRetry remoteMessageRetry;
//
//
//    @Before
//    public void init() {
//        remoteMessageRetry = new RemoteMessageRetry(new RemoteRetryProvider() {
//            @Override
//            public Set<String> getUrls() {
//                return Sets.newHashSet("10.0.7.18:50089");
//            }
//
//            @Override
//            public TransportClient createTransportClient() {
//                return new BrokerTransportClientFactory().create(new ClientConfig());
//            }
//        });
//
//        remoteMessageRetry.start();
//    }
//
//    @Test
//    public void addRetry() throws JournalqException {
//        List<RetryMessageModel> retryMessageModelList = new ArrayList<>();
//
//        RetryMessageModel retry = new RetryMessageModel();
//        retry.setBusinessId("business");
//        retry.setTopic("topic");
//        retry.setApp("app");
//        retry.setPartition((short) 255);
//        retry.setIndex(100l);
//        retry.setBrokerMessage(new byte[168]);
//        retry.setException(new byte[16]);
//        retry.setSendTime(SystemClock.now());
//
//        retryMessageModelList.add(retry);
//
//        remoteMessageRetry.addRetry(retryMessageModelList);
//    }
//
//    @Test
//    public void getRetry() throws JournalqException {
//        String topic = "topic";
//        String app = "app";
//        short count = 10;
//        long startIndex = 0;
//        List<RetryMessageModel> retry = remoteMessageRetry.getRetry(topic, app, count, startIndex);
//        for (RetryMessageModel model : retry) {
//            System.out.println(ToStringBuilder.reflectionToString(model));
//        }
//    }
//
//    @Test
//    public void countRetry() throws JournalqException {
//        remoteMessageRetry.countRetry("topic", "app");
//    }
//
//    @Test
//    public void retrySuccess() throws JournalqException {
//        remoteMessageRetry.retrySuccess("topic", "app", new Long[]{1l});
//    }
//
//    @Test
//    public void retryError() throws JournalqException {
//        remoteMessageRetry.retryError("topic", "app", new Long[]{1l});
//    }
//
//    @Test
//    public void retryExpire() throws JournalqException {
//        remoteMessageRetry.retryExpire("topic", "app", new Long[]{1l});
//    }

}