/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.joyqueue.producer.support;

import io.openmessaging.Future;
import io.openmessaging.FutureListener;

/**
 * FutureAdapter
 *
 * author: gaohaoxiang
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

        FutureValueHolder(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }
    }
}