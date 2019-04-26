package com.jd.journalq.nsr.utils;

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
import java.util.concurrent.*;

public class AsyncHttpClient {
    private static CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(600)
            .setSocketTimeout(700)
            .setConnectionRequestTimeout(500).build()).build();
    static {
        httpclient.start();
    }
    private static <T> Future<T> asyncRequest(HttpUriRequest request, Class<T> clazz){
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
    public static <T> Future<T> post(String host,String path,String body,Class<T> clazz) throws Exception{
        HttpPost post=new HttpPost(host+path);
        post.setEntity(new StringEntity(body));
        return asyncRequest(post,clazz);
    }

    /**
     * Http get
     *
     *
     **/
    public static <T> Future<T> get(String host,String path,String body,Class<T> clazz) throws Exception{
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
        public BasicFuture() {
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
            return result;
        }
    }

    public static void close() throws IOException {
        if(httpclient.isRunning()) {
            httpclient.close();
        }
    }



}
