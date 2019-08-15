package io.chubao.joyqueue.toolkit.lang;

import java.util.List;

/**
 * ListUtil
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class ListUtil {

    public static Long[] toArray(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return new Long[0];
        }
        Long[] result = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}