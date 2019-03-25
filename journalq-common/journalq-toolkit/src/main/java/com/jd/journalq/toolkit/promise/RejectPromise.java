package com.jd.journalq.toolkit.promise;


import java.util.concurrent.Executor;

/**
 * @author liyue25
 * Date: 2018/10/29
 */
public class RejectPromise<R> extends Promise<R>{
    private final Promise<R> parent;
    private final ExceptionHandler handler;

    public RejectPromise(Promise<R> parent, ExceptionHandler handler) {
        this.parent = parent;
        this.handler = handler;
    }

    @Override
    public void submit(ExceptionHandler exceptionHandler) {
        submit( null, exceptionHandler);
    }

    @Override
    void submit(InputCallbackFunction<R> callback, ExceptionHandler exceptionHandler) {
        parent.submit(callback, (p, e) -> handler.handle(p, e) || exceptionHandler.handle(p, e));
    }

    @Override
    protected Executor executor() {
        return parent.executor();
    }

}
