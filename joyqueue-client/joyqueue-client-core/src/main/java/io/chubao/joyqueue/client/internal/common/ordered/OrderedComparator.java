package io.chubao.joyqueue.client.internal.common.ordered;

import java.util.Comparator;

/**
 * OrderedComparator
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class OrderedComparator implements Comparator {

    private static final OrderedComparator INSTANCE = new OrderedComparator();

    public static OrderedComparator getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(Object o1, Object o2) {
        boolean o1Ordered = (o1 instanceof Ordered);
        boolean o2Ordered = (o2 instanceof Ordered);

        if (!o1Ordered && !o2Ordered) {
            return 0;
        }
        if (o1Ordered && !o2Ordered) {
            return 1;
        }
        if (!o1Ordered && o2Ordered) {
            return -1;
        }

        int o1Order = ((Ordered) o1).getOrder();
        int o2Order = ((Ordered) o2).getOrder();
        return (o1Order < o2Order) ? -1 : ((o1Order == o2Order) ? 0 : 1);
    }
}