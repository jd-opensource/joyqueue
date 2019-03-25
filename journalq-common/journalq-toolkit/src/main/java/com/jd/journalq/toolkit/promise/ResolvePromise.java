package com.jd.journalq.toolkit.promise;


import java.util.concurrent.Executor;

/**
 * @author liyue25
 * Date: 2018/10/29
 */
public class ResolvePromise<P, R> extends Promise<R> {
    private Promise<P> parent;
    private InputOutputResolveFunction<P, R> resolveFunction;
    private final Executor executor;
    ResolvePromise(Promise<P> parent, InputOutputResolveFunction<P, R> resolveFunction, Executor executor) {
        this.parent = parent;
        this.resolveFunction = resolveFunction;
        this.executor = executor;
    }

    @Override
    public void submit(ExceptionHandler exceptionHandler) {
        submit( null, exceptionHandler);
    }

    @Override
    void submit(InputCallbackFunction<R> callback, ExceptionHandler exceptionHandler){
        if (null != parent) {
            parent.submit(p -> executor().execute(() -> resolve(callback, exceptionHandler, p)),
                    (pp,pe) -> reject(pp, exceptionHandler, pe));
        } else {
            executor().execute(() -> resolve(callback, exceptionHandler,null));
        }
    }

    private void resolve(InputCallbackFunction<R> callback, ExceptionHandler exceptionHandler, P p) {
        try {
            R r = resolveFunction.resolve(p);
            if(null != callback) callback.callback(r);
        } catch (Exception e) {
            reject(p, exceptionHandler, e);
        }
    }

    @Override
    protected Executor executor() {
        return executor ==null ? parent.executor(): executor;
    }

}
