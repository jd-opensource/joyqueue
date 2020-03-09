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

import org.joyqueue.model.exception.BusinessException;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 *  HTTP服务 公共方法
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    protected static final long DEFAULT_HTTP_CONN_TIME_TO_LIVE = 60;
    protected static final int DEFAULT_HTTP_CONN_MAX_TOTAL = 10;
    /**
     * http client 构建器
     */
    private static HttpClientBuilder httpClientBuilder;
    /**
     * 链接管理器
     */
    private static PoolingHttpClientConnectionManager clientConnManager;
    private static RequestConfig requestConfig;

    static {
        clientConnManager = new PoolingHttpClientConnectionManager(DEFAULT_HTTP_CONN_TIME_TO_LIVE, TimeUnit.SECONDS);
        clientConnManager.setMaxTotal(DEFAULT_HTTP_CONN_MAX_TOTAL);
        httpClientBuilder = HttpClientBuilder.create().setConnectionManager(clientConnManager);
        requestConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
    }

    public static String createUrl(String url, String uri) {
        return url + uri;
    }

    public static CloseableHttpResponse executeRequest(HttpRequestBase request) {
        try {
            request.setConfig(requestConfig);
            try {
                if (logger.isInfoEnabled()) {
                    String content = "";
                    if (request instanceof HttpEntityEnclosingRequest) {
                        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                        if (entity != null && entity instanceof StringEntity) {
                            if (entity != null) {
                                InputStream stream = entity.getContent();
                                if (stream != null && stream.available() > 0) {
                                    byte[] bytes = new byte[stream.available()];
                                    stream.read(bytes);
                                    content = new String(bytes);
                                }
                            }
                        }
                    }
                    logger.info(String.format("communicating request[%s],entity[%s].", request.toString(), content));
                }
            } catch (Throwable e) {
                logger.warn("logger error.", e);
            }
            long startMs = SystemClock.now();
            CloseableHttpResponse response = getClient().execute(request);
            logger.info(String.format("communicating request[%s],time elapsed %d ms.", request.toString(), SystemClock.now() - startMs));
            return response;
        } catch (Exception e) {
            String errorMsg = String.format("error occurred while communicating with  request = %s", request);
            logger.error(errorMsg, e);
            throw new BusinessException(errorMsg, e);
        }
    }

    /**
     * @param url request monitorUrl
     * @throws Exception when request network failed or response http status is not ok
     * @return string
     **/
    public static String get(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        return processResponse(executeRequest(get), get);
    }


    /**
     * @param url request monitorUrl
     * @throws Exception when request network failed or response http status is not ok
     * @return string
     **/
    public static String put(String url, String content) throws Exception {
        HttpPut put = new HttpPut(url);
        put.setEntity(new StringEntity(content));
        return processResponse(executeRequest(put), put);
    }

    /**
     * @param url request monitorUrl
     * @throws Exception when request network failed or response http status is not ok
     * @return string
     **/
    public static String delete(String url) throws Exception {
        HttpDelete delete = new HttpDelete(url);
        return processResponse(executeRequest(delete), delete);
    }




    /**
     *
     * @param request  http request
     * @param response  http response
     * @throws IllegalAccessException when network response http status is not ok or other network exception
     * @throws IOException when failed to process response entity
     * process http 请求的返回
     * @return string body
     **/
    private static String processResponse(CloseableHttpResponse response, HttpUriRequest request) throws IllegalStateException, IOException {
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                String message = String.format("monitorUrl [%s],reuqest[%s] error code [%s],response[%s]",
                        request.getURI().toString(), request.toString(), statusCode, EntityUtils.toString(response.getEntity()));
                throw new IllegalStateException(message);
            }
            String result = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
            logger.info("request[{}] response[{}]", request.toString(), result);
            return result;
        } finally {
            response.close();
        }
    }

    private static CloseableHttpClient getClient() throws Exception {
        return httpClientBuilder.build();
    }
}
