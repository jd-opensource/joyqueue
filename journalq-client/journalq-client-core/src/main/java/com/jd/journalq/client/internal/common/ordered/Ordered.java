package com.jd.journalq.client.internal.common.ordered;

/**
 * 排序
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/27
 */
public interface Ordered {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int getOrder();
}