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
package org.joyqueue.async;

import org.joyqueue.util.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * default broker info asyncQueryOnBroker future
 *
 **/
public class DefaultBrokerInfoFuture implements Future<Map<String,String>> {

    private Logger logger= LoggerFactory.getLogger(DefaultBrokerInfoFuture.class);
    private CountDownLatch latch;
    private Map<String,String> futureResult;
    private String logKey;
    public DefaultBrokerInfoFuture(CountDownLatch latch,Map<String,String> futureResult,String  logKey){
        this.latch=latch;
        this.futureResult=futureResult;
        this.logKey=logKey;
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount()==0?true:false;
    }

    @Override
    public Map<String, String> get() throws InterruptedException{
        latch.await();
        return futureResult;
    }

    @Override
    public Map<String, String> get(long timeout, TimeUnit unit) {
        if(AsyncHttpClient.await(latch,timeout,unit)){
            logger.info("async query on Broker "+logKey+" request finish!");
        }else {
            logger.info("async query on Broker "+logKey+"  request timeout!");
        }
        return futureResult;
    }
}
