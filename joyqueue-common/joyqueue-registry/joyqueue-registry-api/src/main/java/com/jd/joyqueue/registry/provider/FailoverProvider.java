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
package com.jd.joyqueue.registry.provider;

import com.jd.joyqueue.toolkit.URL;

/**
 * Failover注册中心
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-12 下午5:03
 */
public class FailoverProvider implements AddressProvider {

    protected URL url;

    @Override
    public String type() {
        return "failover";
    }

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
        String address;
        Exception error = null;
        AddressProvider provider;
        String[] urls = URL.split(url.getHost());
        for (String value : urls) {
            try {
                URL ul = URL.valueOf(value).addIfAbsent(url.getParameters());
                // 把父参数带入进去
                provider = ADDRESSPROVIDER.get(ul.getProtocol());
                if (provider != null) {
                    provider.setUrl(ul);
                    address = provider.getAddress();
                    if (address != null && !address.isEmpty()) {
                        return address;
                    }
                }
            } catch (Exception e) {
                error = e;
            }
        }
        if (error != null) {
            throw error;
        }
        return null;
    }
}
