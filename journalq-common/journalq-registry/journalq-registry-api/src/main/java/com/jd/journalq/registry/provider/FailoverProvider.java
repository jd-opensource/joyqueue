package com.jd.journalq.registry.provider;

import com.jd.journalq.toolkit.URL;

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
