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
