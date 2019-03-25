package com.jd.journalq.registry.provider;


import com.jd.journalq.toolkit.UrlAware;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.Type;

/**
 * 注册中心工厂类
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-12 下午4:09
 */
public interface AddressProvider extends Type, UrlAware {
    ExtensionPoint<AddressProvider, String> ADDRESSPROVIDER = new ExtensionPointLazy<>(AddressProvider.class);

    /**
     * 获取地址
     *
     * @return 地址
     */
    String getAddress() throws Exception;

}
