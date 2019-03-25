package com.jd.journalq.common.network.transport.command.support;

import com.jd.journalq.common.network.transport.command.Ordered;

import java.util.Comparator;

/**
 * 命令处理顺序比较
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/27
 */
public class CommandHandlerFilterComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Ordered) && !(o2 instanceof Ordered)) {
            return 0;
        }
        if ((o1 instanceof Ordered) && !(o2 instanceof Ordered)) {
            return -1;
        }
        if (!(o1 instanceof Ordered) && (o2 instanceof Ordered)) {
            return 1;
        }

        Ordered order1 = (Ordered) o1;
        Ordered order2 = (Ordered) o2;
        return Integer.compare(order1.getOrder(), order2.getOrder());
    }
}