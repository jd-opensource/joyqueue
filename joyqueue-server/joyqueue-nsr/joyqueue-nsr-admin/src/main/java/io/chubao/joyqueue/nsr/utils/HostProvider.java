package io.chubao.joyqueue.nsr.utils;

public interface HostProvider {
    int size();
    String next(long spinMs);
    void onConnected();
}
