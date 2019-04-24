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
