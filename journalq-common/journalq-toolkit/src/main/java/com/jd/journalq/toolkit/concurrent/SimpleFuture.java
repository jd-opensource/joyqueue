/**
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
package com.jd.journalq.toolkit.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SimpleFuture
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/10
 */
public class SimpleFuture<T> implements Future<T> {

    private volatile boolean done = false;
    private volatile ResponseHolder<T> responseHolder;
    private volatile Throwable cause;

    public void setThrowable(Throwable cause) {
        this.cause = cause;
        done = true;
        doNotify();
    }

    public void setResponse(T response) {
        responseHolder = new ResponseHolder(response);
        done = true;
        doNotify();
    }

    protected void doNotify() {
        synchronized (this) {
            notifyAll();
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
        return done;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (done) {
            return responseHolder.getResponse();
        }
        synchronized (this) {
            wait();
        }
        return getFutureResponse();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (done) {
            return responseHolder.getResponse();
        }
        synchronized (this) {
            wait(unit.toMillis(timeout));
        }
        if (!done) {
            throw new TimeoutException();
        }
        return getFutureResponse();
    }

    protected T getFutureResponse() throws ExecutionException {
        if (cause != null) {
            throw new ExecutionException(cause);
        }
        return responseHolder.getResponse();
    }

    private class ResponseHolder<T> {
        private T response;

        private ResponseHolder(T response) {
            this.response = response;
        }

        public void setResponse(T response) {
            this.response = response;
        }
        public T getResponse() {
            return response;
        }
    }
}