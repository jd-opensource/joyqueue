package com.jd.journalq.client.internal.common.ordered;

import java.util.Collections;
import java.util.List;

/**
 * OrderedSorter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class OrderedSorter {

    public static <T> List<T> sort(List<T> list) {
        Collections.sort(list, OrderedComparator.getInstance());
        return list;
    }
}