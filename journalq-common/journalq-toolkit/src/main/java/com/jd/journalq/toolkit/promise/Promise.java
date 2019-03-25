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
