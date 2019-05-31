package com.jd.journalq.nsr.utils;

public interface HostProvider {
    int size();
    String next(long spinMs);
    void onConnected();
}
