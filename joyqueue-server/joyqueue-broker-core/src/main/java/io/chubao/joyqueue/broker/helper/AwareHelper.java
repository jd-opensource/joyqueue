package io.chubao.joyqueue.broker.helper;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;

import java.util.List;

/**
 * AwareHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class AwareHelper {

    public static <T> List<T> enrichIfNecessary(List<T> list, BrokerContext brokerContext) {
        if (list == null) {
            return list;
        }
        for (T obj : list) {
            enrichIfNecessary(obj, brokerContext);
        }
        return list;
    }

    public static <T> T enrichIfNecessary(T obj, BrokerContext brokerContext) {
        if (obj == null) {
            return obj;
        }
        if (obj instanceof PropertySupplierAware) {
            ((PropertySupplierAware) obj).setSupplier(brokerContext.getPropertySupplier());
        }

        if (obj instanceof BrokerContextAware) {
            ((BrokerContextAware) obj).setBrokerContext(brokerContext);
        }
        return obj;
    }
}