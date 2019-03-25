package io.openmessaging.journalq.producer.support;

import io.openmessaging.Future;
import io.openmessaging.FutureListener;

/**
 * FutureAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class FutureAdapter<V> implements Future<V> {

    private Throwable throwable;
    private FutureValueHolder<V> valueHolder;
    private FutureListener listener;

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        doNotify();
    }

    public void setValue(Object value) {
        valueHolder = new FutureValueHolder(value);
        doNotify();
    }

    protected V getValue() {
        if (valueHolder == null) {
            return null;
        }
        return valueHolder.getValue();
    }

    protected void doNotify() {
        if (listener != null) {
            listener.operationComplete(this);
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return (valueHolder != null || throwable != null);
    }

    @Override
    public V get() {
        if (isDone()) {
            return getValue();
        }
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        return getValue();
    }

    @Override
    public V get(long timeout) {
        if (isDone()) {
            return getValue();
        }
        synchronized (this) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
            }
        }
        return getValue();
    }

    @Override
    public void addListener(FutureListener listener) {
        this.listener = listener;
        if (isDone()) {
            listener.operationComplete(this);
        }
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    class FutureValueHolder<V> {

        private V value;

        public FutureValueHolder(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }
    }
}