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
package com.jd.journalq.toolkit.promise;


import java.util.concurrent.Executor;

/**
 * @author liyue25
 * Date: 2018/10/29
 */
public abstract class Promise<R>{

    public <NR> Promise<NR> then(Executor executor, InputOutputResolveFunction<R, NR> resolveFunction) {
        return new ResolvePromise<>(this, resolveFunction, executor);
    }

    public NoOutputPromise then(Executor executor, InputResolveFunction<R> resolveFunction) {
        return new NoOutputResolvePromise<>(this, resolveFunction, executor);
    }

    public <NR> Promise<NR> then(InputOutputResolveFunction<R, NR> resolveFunction) {
        return new ResolvePromise<>(this, resolveFunction, null);
    }

    public NoOutputPromise then(InputResolveFunction<R> resolveFunction) {
        return new NoOutputResolvePromise<>(this, resolveFunction, null);
    }

    public  RejectPromise<R> handle(ExceptionHandler exceptionHandler) {
        return new RejectPromise<>(this, exceptionHandler);
    }

    public  RejectPromise<R> handle(IgnoreExceptionHandler exceptionHandler) {
        return new RejectPromise<>(this, exceptionHandler);
    }

    public  RejectPromise<R> handleTerminate(TerminateExceptionHandler exceptionHandler) {
        return new RejectPromise<>(this, exceptionHandler);
    }

    public  RejectPromise<R> handleTerminate(TerminateIgnoreExceptionHandler exceptionHandler) {
        return new RejectPromise<>(this, exceptionHandler);
    }

    public void submit() {
        submit( (p, e) -> true);
    }
    public abstract void submit( ExceptionHandler exceptionHandler);

    abstract void submit(InputCallbackFunction<R> callback, ExceptionHandler exceptionHandler);

    boolean reject(Object parameter, ExceptionHandler exceptionHandler, Exception e) {
        if (exceptionHandler != null) {
            return exceptionHandler.handle(parameter, e);
        } else {
            return true;
        }
    }

    public static <R> Promise<R> promise(Executor executor, OutputResolveFunction<R> sourceFunction) {
        return new ResolvePromise<>(null, sourceFunction, executor);
    }

    public static NoOutputPromise promise(Executor executor, ResolveFunction sourceFunction) {
        return new NoOutputResolvePromise<>(null, sourceFunction, executor);
    }

    protected abstract Executor executor();

}
