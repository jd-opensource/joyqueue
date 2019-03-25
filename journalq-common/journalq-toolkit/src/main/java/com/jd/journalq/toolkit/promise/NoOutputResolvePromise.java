package com.jd.journalq.toolkit.promise;


import java.util.concurrent.Executor;

/**
 * @author liyue25
 * Date: 2018/10/29
 */
public class NoOutputResolvePromise<P> extends NoOutputPromise {
    private Promise<P> parent;
    private InputOutputResolveFunction<P, Void> resolveFunction;
    private final Executor executor;
    NoOutputResolvePromise(Promise<P> parent, InputOutputResolveFunction<P, Void> resolveFunction, Executor executor) {
        this.parent = parent;
        this.resolveFunction = resolveFunction;
        this.executor = executor;
    }



    private void resolve(CallbackFunction callback, ExceptionHandler exceptionHandler, P p) {
        try {
            resolveFunction.resolve(p);
            if(null != callback) callback.callbackNoInput();
        } catch (Exception e) {
            reject(p, exceptionHandler, e);
        }
    }


    @Override
    void submit(CallbackFunction callback, ExceptionHandler exceptionHandler) {


        if (null != parent) {
            parent.submit(
                    p -> executor().execute(() -> resolve(callback, exceptionHandler, p)),
                    (p, e) -> reject(p, exceptionHandler, e));
        } else {
            executor().execute(() -> resolve(callback, exceptionHandler,null));
        }
    }

    @Override
    public void submit(ExceptionHandler exceptionHandler) {
        submit(null, exceptionHandler);
    }

    @Override
    protected Executor executor() {
        return executor ==null ? parent.executor(): executor;
    }


}
