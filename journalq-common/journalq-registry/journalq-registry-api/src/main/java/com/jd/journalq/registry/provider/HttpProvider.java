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
package com.jd.journalq.registry.provider;

import com.jd.journalq.toolkit.URL;
import com.jd.journalq.toolkit.network.http.Get;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.retry.Retry;
import com.jd.journalq.toolkit.retry.RetryPolicy;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * HTTP远程请求注册中心
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-12 下午5:03
 */
public class HttpProvider implements AddressProvider {

    public static final String SOCKET_TIMEOUT = "timeout";
    public static final String RETRY_TIMES = "retryTimes";
    public static final String CHARSET = "charset";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 1000;
    public static final int DEFAULT_RETRY_TIMES = 1;

    protected URL url;

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String getAddress() throws Exception {
        if (url == null) {
            throw new IllegalStateException("url is null");
        }
        if (url.getHost() == null) {
            throw new IllegalStateException("url is invalid");
        }
        int retryTimes = url.getPositive(RETRY_TIMES, DEFAULT_RETRY_TIMES);
        final int connectionTimeout = url.getPositive(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        final int socketTimeout = url.getPositive(SOCKET_TIMEOUT, DEFAULT_TIMEOUT);
        final String charset = url.getString(CHARSET, Charsets.UTF_8.name());
        final URL ul = url.remove(RETRY_TIMES, CONNECTION_TIMEOUT, SOCKET_TIMEOUT, CHARSET);

        String html = Retry.execute(new RetryPolicy(500, retryTimes), new Callable<String>() {
            @Override
            public String call() throws Exception {
                return html(ul, charset, connectionTimeout, socketTimeout);
            }
        });
        if (html == null || html.contains("<")) {
            return null;
        }

        return html;
    }

    /**
     * 获取html内容
     *
     * @param url               地址
     * @param charset           字符集
     * @param connectionTimeout 连接超时
     * @param socketTimeout     读取时间
     * @return
     * @throws IOException
     */
    protected String html(final URL url, final String charset, final int connectionTimeout,
                          final int socketTimeout) throws IOException {
        return Get.Builder.build().charset(charset).connectionTimeout(connectionTimeout).socketTimeout(socketTimeout)
                .create().get(url);
    }

    @Override
    public String type() {
        return "http";
    }
}
