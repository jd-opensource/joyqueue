package com.jd.journalq.store.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author liyue25
 * Date: 2019-01-17
 */
public class ThreadSafeFormat {
    private static ThreadLocal<SimpleDateFormat> sdfHolder
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    public static String format(Date date) {
        return sdfHolder.get().format(date);
    }

    public static String formatWithComma(long position) {
        return  NumberFormat.getNumberInstance(Locale.US).format(position);
    }
}
