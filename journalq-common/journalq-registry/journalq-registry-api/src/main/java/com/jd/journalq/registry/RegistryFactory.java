package com.jd.journalq.registry;

import com.jd.journalq.toolkit.URL;
import com.jd.journalq.registry.provider.AddressProvider;
import com.jd.journalq.toolkit.UrlAware;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 注册中心代理
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 13-1-15 下午3:37
 */
public class RegistryFactory {
    ExtensionPoint<Registry, String> REGISTRYPROVIDER = new ExtensionPointLazy<>(Registry.class);
    //注册中心URL地址
    protected String url;
    //存活节点
    protected String live;
    //初始化等待时间，单位毫秒
    protected long waitTime = 5000;

    public RegistryFactory() {
    }

    public RegistryFactory(String url) {
        this(url, null, 5000);
    }

    public RegistryFactory(String url, String live) {
        this(url, live, 5000);
    }

    public RegistryFactory(String url, String live, long waitTime) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("address is null");
        }
        this.url = url;
        this.live = live;
        this.waitTime = waitTime;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLive(String live) {
        this.live = live;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * 创建并启动注册中心
     *
     * @return 注册中心
     * @throws Exception
     */
    public Registry create() throws Exception {
        if (url == null) {
            throw new IllegalStateException("url is null");
        }
        String address = url;
        URL ul = URL.valueOf(address);
        // 创建注册中心
        Registry registry = REGISTRYPROVIDER.get(ul.getProtocol());

        if (registry == null) {
            // 判断是否是地址提供者
            AddressProvider factory = AddressProvider.ADDRESSPROVIDER.get(ul.getProtocol());
            if (factory != null) {
                if (factory instanceof UrlAware) {
                    factory.setUrl(ul);
                }
                // 获取地址
                address = factory.getAddress();
                if (address != null && !address.isEmpty()) {
                    ul = URL.valueOf(address);
                    registry = REGISTRYPROVIDER.get(ul.getProtocol());
                }
            }
        }
        if (registry == null) {
            throw new IllegalStateException("registry is null," + url);
        } else if (registry instanceof UrlAware) {
            ((UrlAware) registry).setUrl(ul);
        }
        // 启动
        registry.start();
        if (live != null && !live.isEmpty()) {
            registry.createLive(live, null);
        }
        if (waitTime > 0) {
            // 延迟等待初始化数据
            CountDownLatch latch = new CountDownLatch(1);
            try {
                latch.await(waitTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        return registry;
    }
}
