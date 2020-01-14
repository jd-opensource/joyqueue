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
package org.joyqueue.toolkit.service;

import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.validate.Validators;
import org.joyqueue.toolkit.validate.annotation.Valid;

import java.lang.annotation.Annotation;

/**
 * 服务
 */
public abstract class Service extends Activity implements LifeCycle {

    @Override
    protected void validate() throws Exception {
        super.validate();
        // 如果类上配置了验证，则验证该类的字段
        Annotation valid = this.getClass().getAnnotation(Valid.class);
        if (valid != null) {
            Validators.validate(this);
        }
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    @Override
    public void stop() {
        super.stop(null);
    }

    @Override
    public void stop(final Runnable runnable) {
        super.stop(runnable);
    }

    @Override
    public void willStop() {
        super.willStop();
    }

    @Override
    public ServiceState getServiceState() {
        return super.getServiceState();
    }

    @Override
    public boolean isStarted() {
        return super.isStarted();
    }

    @Override
    public boolean isStopped() {
        return super.isStopped();
    }

    @Override
    public boolean isReady() {
        return super.isReady();
    }

    /**
     * 选举服务标示
     * Created by hexiaofeng on 16-8-25.
     */
    public interface Election {

        /**
         * 注册，参与选举
         */
        void register(ElectionListener listener);

        /**
         * 注销，取消选举
         */
        void deregister();

        /**
         * 是否是领导
         *
         * @return 领导标示
         */
        boolean isLeader();

    }

    /**
     * 选举监听器
     */
    public interface ElectionListener {

        /**
         * 获取领导事件
         */
        void onTake();

        /**
         * 丢失领导事件
         */
        void onLost();

    }

}
