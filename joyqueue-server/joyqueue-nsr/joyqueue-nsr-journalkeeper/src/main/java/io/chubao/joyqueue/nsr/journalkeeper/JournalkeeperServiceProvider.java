package io.chubao.joyqueue.nsr.journalkeeper;

import io.chubao.joyqueue.nsr.ServiceProvider;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.service.Service;

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