package com.jd.journalq.nsr;

public interface ServiceProvider {
    <T> T getService(final Class<T> clazz);
}
