package io.chubao.joyqueue.nsr.journalkeeper.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * ArrayHelper
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class ArrayHelper {

    private static final String DEFAULT_SPLITTER = ",";

    public static List<Integer> toIntList(String value) {
        return toIntList(value, DEFAULT_SPLITTER);
    }

    public static List<Integer> toIntList(String value, String splitter) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        String[] splitValue = value.split(splitter);
        if (ArrayUtils.isEmpty(splitValue)) {
            return Collections.emptyList();
        }
        List<Integer> result = Lists.newArrayList();
        for (String item : splitValue) {
            result.add(Integer.valueOf(item));
        }
        return result;
    }

    public static Set<Integer> toIntSet(String value) {
        return toIntSet(value, DEFAULT_SPLITTER);
    }

    public static Set<Integer> toIntSet(String value, String splitter) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptySet();
        }
        String[] splitValue = value.split(splitter);
        if (ArrayUtils.isEmpty(splitValue)) {
            return Collections.emptySet();
        }
        Set<Integer> result = Sets.newHashSet();
        for (String item : splitValue) {
            result.add(Integer.valueOf(item));
        }
        return result;
    }

    public static Set<Short> toShortSet(String value) {
        return toShortSet(value, DEFAULT_SPLITTER);
    }

    public static Set<Short> toShortSet(String value, String splitter) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptySet();
        }
        String[] splitValue = value.split(splitter);
        if (ArrayUtils.isEmpty(splitValue)) {
            return Collections.emptySet();
        }
        Set<Short> result = Sets.newHashSet();
        for (String item : splitValue) {
            result.add(Short.valueOf(item));
        }
        return result;
    }

    public static String toString(Collection collection) {
        return toString(collection, DEFAULT_SPLITTER);
    }

    public static String toString(Collection collection, String splitter) {
        if (CollectionUtils.isEmpty(collection)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Object item : collection) {
            result.append(String.valueOf(item));
            result.append(splitter);
        }
        return result.substring(0, result.length() - 1);
    }
}