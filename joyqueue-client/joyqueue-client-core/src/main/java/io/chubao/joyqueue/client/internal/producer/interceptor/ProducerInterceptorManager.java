package io.chubao.joyqueue.client.internal.producer.interceptor;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.common.ordered.OrderedSorter;

import java.util.List;

/**
 * ProducerInterceptorManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class ProducerInterceptorManager {

    private List<ProducerInterceptor> interceptors = Lists.newLinkedList();
    private List<ProducerInterceptor> sortedInterceptors;

    public List<ProducerInterceptor> getSortedInterceptors() {
        if (sortedInterceptors != null) {
            return sortedInterceptors;
        }
        List<ProducerInterceptor> sortedInterceptors = Lists.newLinkedList();
        sortedInterceptors.addAll(GlobalProducerInterceptorManager.getInterceptors());
        sortedInterceptors.addAll(interceptors);
        OrderedSorter.sort(sortedInterceptors);
        this.sortedInterceptors = sortedInterceptors;
        return sortedInterceptors;
    }

    public void addInterceptor(ProducerInterceptor interceptor) {
        interceptors.add(interceptor);
        sortedInterceptors = null;
    }

    public void removeInterceptor(ProducerInterceptor interceptor) {
        interceptors.remove(interceptor);
        sortedInterceptors = null;
    }
}