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
package org.joyqueue.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.joyqueue.exception.ServiceException;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AsyncHttpClient {

    private static final int ASYNC_TIMEOUT = 2000;
    private static final int MAX_CONN_TOTAL = 20;
    private static final int MAX_CONN_PER_ROUTE = 10;
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClient.class);
    private static CloseableHttpAsyncClient httpclient;

    static {
        httpclient = HttpAsyncClients.custom()
                .setMaxConnTotal(MAX_CONN_TOTAL)
                .setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(ASYNC_TIMEOUT)
                        .setSocketTimeout(ASYNC_TIMEOUT)
                        .setConnectionRequestTimeout(ASYNC_TIMEOUT).build()).build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (null != httpclient) {
                try {
                    httpclient.close();
                    logger.info("close async http client success.");
                } catch (IOException e) {
                    logger.error("close async http client error.", e);
                    httpclient = null;
                }
            }
        }));
    }

    public static void AsyncRequest(HttpUriRequest request, FutureCallback<HttpResponse> asyncCallBack){
         httpclient.start();
         request.setHeader("Content-Type", "application/json;charset=utf-8");
         httpclient.execute(request,asyncCallBack);
    }

    @Deprecated
    public static class ConcurrentResponseHandler implements FutureCallback<HttpResponse>{
        private Logger logger = LoggerFactory.getLogger(ConcurrentResponseHandler.class);
        public CountDownLatch latch;
        private List<String> result;
        private Object object = new Object();
        public ConcurrentResponseHandler(CountDownLatch latch){
            this.latch = latch;
            this.result = new ArrayList<>(8);
        }
        @Override
        public void completed(HttpResponse httpResponse) {
            try {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (HttpStatus.SC_OK == statusCode) {
                     String response = EntityUtils.toString(httpResponse.getEntity());
//                     logger.info(response);
                     synchronized (object) {
                         result.add(response);
                     }
                }
            } catch (IOException e){
                logger.info("network io exception",e);
            } finally {
                latch.countDown();
                HttpClientUtils.closeQuietly(httpResponse);
            }
        }

        public List<String> getResult(){
          return result;
        }

        @Override
        public void failed(Exception e) {
             logger.info("request failed",e);
             latch.countDown();
        }

        @Override
        public void cancelled() {
            logger.info("request cancel");
             latch.countDown();
        }
    }


    /**
     *
     * static inner class, can be instance by new AsyncHttpClient.ConcurrentHttpResponseHandler(...)
     **/
    public static class ConcurrentHttpResponseHandler implements FutureCallback<HttpResponse>{
        private Logger logger= LoggerFactory.getLogger(ConcurrentHttpResponseHandler.class);
        public CountDownLatch latch;
        private Map<String,String> result;
        private String requestKey;
        private String url;
        /**
         * 开始时间
         */
        private long startMs;
        public ConcurrentHttpResponseHandler(String url,long startMs,CountDownLatch latch, String requestKey, Map<String,String> result){
            this.latch=latch;
            this.result=result;
            this.requestKey=requestKey;
            this.url = url;
            this.startMs = startMs;
        }
        @Override
        public void completed(HttpResponse httpResponse) {
            logger.info("request completed {} time elapsed {} ms ",url, SystemClock.now()-startMs);
            try {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (HttpStatus.SC_OK == statusCode) {
                    String response = EntityUtils.toString(httpResponse.getEntity());
//                    logger.info(response);
                    result.put(requestKey,response);
                }else{
                    logger.info("response but http status not 200");
                }
            }catch (IOException e){
                logger.info("network io exception",e);
            }finally {
                latch.countDown();
            }
        }

        @Override
        public void failed(Exception e) {
            logger.info(String.format("request failed %s",requestKey),e);
            latch.countDown();
        }

        @Override
        public void cancelled() {
            logger.info(String.format("request canceled %s",requestKey));
            latch.countDown();
        }
    }

    public static void main(String[] args){
        int concurrency=5;
        CountDownLatch latch=new CountDownLatch(5);
        String host="http://localhost:10030/v1/monitor/app/%d/topic/%d/app/%d/%s";
        ConcurrentResponseHandler handler=new ConcurrentResponseHandler(latch);
        for(int i=0;i<concurrency;i++){
            AsyncHttpClient.AsyncRequest(new HttpGet(String.format(host,i,i,i,"topcic"))
                    ,handler);
        }
        logger.info("request finish,and wait result");
        try {
            latch.await();
        }catch (InterruptedException e){
            logger.info("interrupted",e);
        }
        List<String>  results= handler.getResult();
        logger.info(JSON.toJSONString(results));
    }

    /**
     *
     * 同步等待
     *
     **/
    public static boolean await(CountDownLatch latch, long timeout, TimeUnit unit){
        try {
            return latch.await(timeout, unit);
        }catch (InterruptedException e){
            String errorMsg = "async asyncQueryOnBroker broker info interrupted.";
            logger.error(errorMsg, e);
            throw new ServiceException(ServiceException.INTERNAL_SERVER_ERROR, errorMsg);
        }
    }
}
