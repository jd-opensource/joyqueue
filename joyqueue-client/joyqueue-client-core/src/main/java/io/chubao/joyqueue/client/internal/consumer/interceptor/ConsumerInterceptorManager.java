package io.chubao.joyqueue.client.internal.consumer.interceptor;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.common.ordered.OrderedSorter;

import java.util.List;

/**
 * ConsumerInterceptorManager
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class ConsumerInterceptorManager {

    private List<ConsumerInterceptor> interceptors = Lists.newLinkedList();
    private List<ConsumerInterceptor> sortedInterceptors;

    public List<ConsumerInterceptor> getSortedInterceptors() {
        if (sortedInterceptors != null) {
            return sortedInterceptors;
        }
        List<ConsumerInterceptor> sortedInterceptors = Lists.newLinkedList();
        sortedInterceptors.addAll(GlobalConsumerInterceptorManager.getInterceptors());
        sortedInterceptors.addAll(interceptors);
        OrderedSorter.sort(sortedInterceptors);
        this.sortedInterceptors = sortedInterceptors;
        return sortedInterceptors;
    }

    public void addInterceptor(ConsumerInterceptor interceptor) {
        interceptors.add(interceptor);
        sortedInterceptors = null;
    }

    public void removeInterceptor(ConsumerInterceptor interceptor) {
        interceptors.remove(interceptor);
        sortedInterceptors = null;
    }
}