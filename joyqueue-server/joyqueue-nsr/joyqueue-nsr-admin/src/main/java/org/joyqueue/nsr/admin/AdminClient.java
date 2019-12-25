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

import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.nsr.NsrAdmin;
import org.joyqueue.nsr.utils.AsyncHttpClient;
import org.joyqueue.nsr.utils.HostProvider;
import org.joyqueue.nsr.utils.RoundRobinHostProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class AdminClient implements NsrAdmin {
    private Logger logger= LoggerFactory.getLogger(AdminClient.class);
    private final long DELAY_MS=100;
    private AppAdmin appAdmin;
    private TopicAdmin topicAdmin;
    private BrokerAdmin brokerAdmin;
    private String host;
    private HostProvider hostProvider;
    AsyncHttpClient httpClient;
    public AdminClient(String connectionStr){
        this.hostProvider=new RoundRobinHostProvider(connectionStr);
        this.host=hostProvider.next(DELAY_MS);
        this.httpClient=new AsyncHttpClient();
        this.appAdmin=new AppAdmin(httpClient);
        this.topicAdmin =new TopicAdmin(httpClient);
        this.brokerAdmin=new BrokerAdmin(httpClient);
    }
    @Override
    public String publish(TopicAdmin.PublishArg pubSubArg) throws Exception{
        pubSubArg.host=host;
        try {
            String result= topicAdmin.publish(pubSubArg, null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw  e;
        }

    }

    @Override
    public String subscribe(TopicAdmin.SubscribeArg pubSubArg) throws Exception{
        pubSubArg.host=host;

        try {
            String result=  topicAdmin.subscribe(pubSubArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw  e;
        }

    }

    @Override
    public String unPublish(TopicAdmin.PublishArg pubSubArg) throws Exception {
        pubSubArg.host=host;

        try {
            String result= topicAdmin.unPublish(pubSubArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw  e;
        }

    }

    @Override
    public String unSubscribe(TopicAdmin.SubscribeArg pubSubArg) throws Exception {
        pubSubArg.host=host;
        try {
            String result= topicAdmin.unSubscribe(pubSubArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw e;
        }
    }

    @Override
    public String delTopic(TopicAdmin.TopicArg topicArg) throws Exception {
        topicArg.host=host;
        try {
            String result= topicAdmin.delete(topicArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw e;
        }
    }

    @Override
    public String createTopic(TopicAdmin.TopicArg topicArg) throws Exception{
        topicArg.host=host;
        try {
            String result= topicAdmin.add(topicArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw  e;
        }
    }

    @Override
    public String token(AppAdmin.TokenArg tokenArg) throws Exception{
        tokenArg.host=host;
        try {
            String result= appAdmin.token(tokenArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw  e;
        }
    }


    @Override
    public List<AppToken> tokens(AppAdmin.TokensArg tokensArg) throws Exception {
        tokensArg.host=host;
        try {
            List<AppToken> result= appAdmin.tokens(tokensArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw  e;
        }
    }

    @Override
    public List<Broker> listBroker(BrokerAdmin.ListArg listArg) throws Exception {
        listArg.host=host;
        try {
            List<Broker> result= brokerAdmin.list(listArg,null);
            hostProvider.onConnected();
            return  result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw e;
        }
    }

    @Override
    public String partitionGroup(TopicAdmin.PartitionGroupArg partitionGroupArg) throws Exception {
        partitionGroupArg.host=host;

        try {
            String result= topicAdmin.partitionGroups(partitionGroupArg,null);
            hostProvider.onConnected();
            return result;
        }catch (Exception e){
            onException();
            logger.info("request exception",e);
            throw e;
        }
    }

    /**
     * Exception event
     **/
    public void onException(){
        this.host=hostProvider.next(DELAY_MS);
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
