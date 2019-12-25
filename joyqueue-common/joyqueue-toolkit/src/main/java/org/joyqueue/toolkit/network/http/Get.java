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
package org.joyqueue.toolkit.network.http;

import org.joyqueue.toolkit.URL;
import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * HTTP工具
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-13 下午1:14
 */
public class Get {

    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int DEFAULT_RETRY_TIMES = 1;

    // 重试次数
    protected int retryTimes;
    // 连接超时
    protected int connectionTimeout;
    // 读取数据超时
    protected int socketTimeout;
    // 字符集
    protected String charset;

    public Get() {
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * 获取网页数据
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String get(final URL url) throws IOException {
        if (url == null) {
            return null;
        }
        if (retryTimes < 0) {
            retryTimes = 0;
        }
        if (socketTimeout < 0) {
            socketTimeout = DEFAULT_TIMEOUT;
        }
        if (connectionTimeout < 0) {
            connectionTimeout = DEFAULT_TIMEOUT;
        }
        if (charset == null || charset.isEmpty()) {
            charset = Charsets.UTF_8.name();
        }

        HttpURLConnection cnn = null;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        for (int i = 0; i < retryTimes + 1; i++) {
            try {
                java.net.URL ul = new java.net.URL(url.toString(true, true));
                cnn = (HttpURLConnection) ul.openConnection();
                cnn.setConnectTimeout(connectionTimeout);
                cnn.setReadTimeout(socketTimeout);
                cnn.setRequestProperty("charset", charset);
                cnn.setUseCaches(false);

                //连接
                cnn.connect();
                if (cnn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    if (cnn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        //页面不存在
                        i = retryTimes;
                    }
                    throw new IOException("http error,code=" + cnn.getResponseCode());
                }

                //获取数据
                reader = new BufferedReader(new InputStreamReader(cnn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            } catch (IOException e) {
                if (i >= retryTimes) {
                    throw new IOException(url.toString(), e);
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
                if (cnn != null) {
                    cnn.disconnect();
                }
            }
        }
        return null;

    }

    /**
     * 构造器
     */
    public static class Builder {
        // 重试次数
        protected int retryTimes = DEFAULT_RETRY_TIMES;
        // 连接超时
        protected int connectionTimeout = DEFAULT_TIMEOUT;
        // 读取数据超时
        protected int socketTimeout = DEFAULT_TIMEOUT;
        // 字符集
        protected String charset = Charsets.UTF_8.name();

        public static Builder build() {
            return new Builder();
        }

        /**
         * 构造
         *
         * @return
         */
        public Get create() {
            Get http = new Get();
            http.setCharset(charset);
            http.setRetryTimes(retryTimes);
            http.setConnectionTimeout(connectionTimeout);
            http.setSocketTimeout(socketTimeout);
            return http;
        }

        public Builder retryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder socketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }
    }
}
