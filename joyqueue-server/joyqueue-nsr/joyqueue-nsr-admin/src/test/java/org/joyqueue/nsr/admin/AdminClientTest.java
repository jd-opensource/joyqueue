/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.nsr.admin;


import com.alibaba.fastjson.JSON;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Subscription;
import org.joyqueue.nsr.admin.AdminClient;
import org.joyqueue.nsr.admin.AppAdmin;
import org.joyqueue.nsr.admin.BrokerAdmin;
import org.joyqueue.nsr.admin.TopicAdmin;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.stream.Collectors;

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminClientTest {
    private static final String host="127.0.0.1:50091";
    private static final String topic="test_topic_11";
    private static final String app="test_app_0";
    static AdminClient client;
    @BeforeClass
    public static void setUp() throws Exception{
        client=new AdminClient(host);
        BrokerAdmin.ListArg listArg=new BrokerAdmin.ListArg();
        TopicAdmin.TopicArg topicArg=new TopicAdmin.TopicArg();
        topicArg.code=topic;
        List<Broker> brokers = client.listBroker(listArg);
        Assert.assertNotNull("no available brokers",brokers);
        topicArg.brokers=brokers.stream().map(broker -> broker.getId()).collect(Collectors.toList());
        client.createTopic(topicArg);
    }

    @Test
    public void appToken() throws Exception{
        AppAdmin.TokenArg tokenArg=new AppAdmin.TokenArg();
        tokenArg.app=this.app;
        String token=client.token(tokenArg);
        Assert.assertNotNull(token);

    }


    @Test
    public void appTokens() throws Exception{
        int tokens=1000;
        AppAdmin.TokensArg tokensArg=new AppAdmin.TokensArg();
        AppAdmin.TokenArg tokenArg=new AppAdmin.TokenArg();
        for(int i=0;i<tokens;i++) {
            String app=this.app + System.currentTimeMillis();
            tokenArg.app = app;
            String token=client.token(tokenArg);
            Assert.assertNotNull(token);
            tokensArg.app = app;
            List<AppToken> appTokens=client.tokens(tokensArg);
            boolean found=false;
            for(AppToken t:appTokens){
                 if(token.equals(t.getToken())){
                     found=true;
                     continue;
                }
            }
            Assert.assertTrue(String.format("app:%s , expected token: %s, ignite tokens: %s",app,token,JSON.toJSONString(appTokens)),found);
        }

    }

    @Test
    public void partitionGroup() throws Exception{
        TopicAdmin.PartitionGroupArg arg=new TopicAdmin.PartitionGroupArg();
        arg.topic=topic;
        String partitionGroups=client.partitionGroup(arg);
        List<PartitionGroup> partitionGroupList=JSON.parseArray(partitionGroups,PartitionGroup.class);
        Assert.assertNotEquals(null,partitionGroupList);
        System.out.println(partitionGroupList);
    }

    @Test
    public void publish() throws Exception{
        TopicAdmin.PublishArg pubSubArg=new TopicAdmin.PublishArg();
        pubSubArg.subscribe.topic=topic;
        pubSubArg.subscribe.app=app;
        pubSubArg.subscribe.type=(int) Subscription.Type.PRODUCTION.getValue();
        String result=client.publish(pubSubArg);
        Assert.assertEquals("success",result);
        result=client.unPublish(pubSubArg);
        Assert.assertEquals("success",result);
    }



    @Test
    public void subscribe() throws Exception{
        TopicAdmin.SubscribeArg pubSubArg=new TopicAdmin.SubscribeArg();
        pubSubArg.subscribe.topic=topic;
        pubSubArg.subscribe.app=app;
        pubSubArg.subscribe.type=(int)Subscription.Type.CONSUMPTION.getValue();
        String result=client.subscribe(pubSubArg);
        Assert.assertEquals("success",result);
        result=client.unSubscribe(pubSubArg);
        Assert.assertEquals("success",result);
    }



    @AfterClass
    public static void clean() throws Exception{
        // remove topic
        TopicAdmin.TopicArg topicArg=new TopicAdmin.TopicArg();
        topicArg.code=topic;
        String result=client.delTopic(topicArg);
        Assert.assertEquals("success",result);
        client.close();
    }



}
