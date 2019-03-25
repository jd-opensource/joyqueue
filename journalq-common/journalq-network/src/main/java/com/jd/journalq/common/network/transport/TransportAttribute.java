package com.jd.journalq.common.network.transport;

import java.util.Set;

/**
 * 通信属性
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface TransportAttribute {

    <T> T set(Object key, Object value);

    <T> T putIfAbsent(Object key, Object value);

    <T> T get(Object key);

    <T> T remove(Object key);

    boolean contains(Object key);

    Set<Object> keys();
}