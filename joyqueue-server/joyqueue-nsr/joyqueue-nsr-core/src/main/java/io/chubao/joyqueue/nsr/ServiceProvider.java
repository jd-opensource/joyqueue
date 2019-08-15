package io.chubao.joyqueue.nsr;

public interface ServiceProvider {
    <T> T getService(Class<T> clazz);
}
