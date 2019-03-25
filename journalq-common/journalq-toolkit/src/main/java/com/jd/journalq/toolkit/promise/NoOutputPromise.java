package com.jd.journalq.toolkit.promise;


import java.util.concurrent.Executor;

/**
 * @author liyue25
 * Date: 2018/10/30
 */
public abstract class NoOutputPromise extends Promise<Void>{
    public <NR> Promise<NR> then(Executor executor, OutputResolveFunction<NR> resolveFunction) {
        return new ResolvePromise<>(this, resolveFunction, executor);
    }
    public NoOutputPromise then(Executor executor, ResolveFunction resolveFunction) {
        return new NoOutputResolvePromise<>(this, resolveFunction, executor);
    }

    public <NR> Promise<NR> then(OutputResolveFunction<NR> resolveFunction) {
        return new ResolvePromise<>(this, resolveFunction, null);
    }
    public NoOutputPromise then(ResolveFunction resolveFunction) {
        return new NoOutputResolvePromise<>(this, resolveFunction, null);
    }

    abstract void submit(CallbackFunction callback, ExceptionHandler exceptionHandler);
    @Override
    void submit(InputCallbackFunction<Void> callback, ExceptionHandler exceptionHandler) {
        submit(() -> {if(null != callback) callback.callback(null);}, exceptionHandler);
    }
}
