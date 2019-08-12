package com.jd.joyqueue.nsr.journalkeeper;

import com.jd.joyqueue.nsr.ServiceProvider;
import com.jd.joyqueue.toolkit.config.PropertySupplier;
import com.jd.joyqueue.toolkit.config.PropertySupplierAware;
import com.jd.joyqueue.toolkit.service.Service;

/**
 * JournalkeeperServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class JournalkeeperServiceProvider extends Service implements ServiceProvider, PropertySupplierAware {

    @Override
    public void setSupplier(PropertySupplier supplier) {

    }

    @Override
    public <T> T getService(Class<T> clazz) {
        return null;
    }
}