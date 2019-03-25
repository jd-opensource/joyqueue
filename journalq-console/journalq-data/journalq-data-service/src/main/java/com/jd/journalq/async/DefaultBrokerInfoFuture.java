package com.jd.journalq.async;

import com.jd.journalq.util.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;


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
