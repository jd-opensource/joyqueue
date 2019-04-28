package com.jd.journalq.toolkit.doc;

public interface Format<T> {
    String format(String sequenceNum, T t);
}
