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
package org.joyqueue.nsr.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class AsyncHttpClient {
    private  CloseableHttpAsyncClient httpclient;
    public AsyncHttpClient(){
        httpclient= HttpAsyncClients
                    .custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(600)
                    .setSocketTimeout(700)
                    .setConnectionRequestTimeout(500).build()).build();
        httpclient.start();
    }

    private  <T> Future<T> asyncRequest(HttpUriRequest request, Class<T> clazz){
        request.setHeader("Content-Type", "application/json;charset=utf-8");
        BasicFuture<T> futureResult=new BasicFuture();
        httpclient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                int statusCode=result.getStatusLine().getStatusCode();
                try {
                    String response = EntityUtils.toString(result.getEntity());
                    if (HttpStatus.SC_OK == statusCode) {
                        if(clazz.equals(String.class)){
                            futureResult.completed((T)response);
                        }else{
                            futureResult.completed(JSON.parseObject(response,clazz));
                        }
                    }else{

                      Exception e=  new Exception(response);
                      System.out.println(response);
                      failed(e);
                    }
                }catch (Exception e){
                    failed(e);
                }
            }
            @Override
            public void failed(Exception ex) {
                futureResult.failed(ex);
            }
            @Override
            public void cancelled() {
                futureResult.cancelled();
            }
        });
        return futureResult;
    }

    /**
     * Http post
     *
     **/
    public  <T> Future<T> post(String host,String path,String body,Class<T> clazz) throws Exception{
        HttpPost post=new HttpPost(host+path);
        post.setEntity(new StringEntity(body));
        return asyncRequest(post,clazz);
    }

    /**
     * Http get
     *
     *
     **/
    public  <T> Future<T> get(String host,String path,String body,Class<T> clazz) throws Exception{
        HttpGet get=new HttpGet(host+path);
        return asyncRequest(get,clazz);
    }

    /**
     *
     * Future simple implement
     *
     **/
    static class BasicFuture<T>  implements Future<T>,FutureCallback<T>{

        private CountDownLatch latch;
        private volatile T result;
        private volatile Throwable throwable;
        BasicFuture() {
            super();
            latch=new CountDownLatch(1);
        }

        @Override
        public void completed(T result) {
            this.result=result;
            notifyWaiter();
        }

        @Override
        public void failed(Exception ex) {
            this.throwable=ex;
            notifyWaiter();
        }

        @Override
        public void cancelled() {
            notifyWaiter();
        }

        public void notifyWaiter(){
            latch.countDown();
        }
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            notifyWaiter();
            return true;
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
        public T get() throws InterruptedException, ExecutionException {
            if(result==null&&!isDone()){
               latch.await();
            }
            return result;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if(result==null&&!isDone()){
                latch.await(timeout,unit);
            }
            if(!isDone()) throw new TimeoutException("timeout !");
            return result;
        }
    }

    public  void close() throws IOException {
        if(httpclient.isRunning()) {
            httpclient.close();
        }
    }



}
