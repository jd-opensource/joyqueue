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
package org.joyqueue.toolkit.validate;

import org.joyqueue.toolkit.validate.ValidateException;
import org.joyqueue.toolkit.validate.Validators;
import org.joyqueue.toolkit.validate.annotation.DoubleRange;
import org.joyqueue.toolkit.validate.annotation.NotEmpty;
import org.joyqueue.toolkit.validate.annotation.NotNull;
import org.joyqueue.toolkit.validate.annotation.Range;
import org.joyqueue.toolkit.validate.annotation.Size;
import org.joyqueue.toolkit.validate.annotation.Valid;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.ValidationException;
import java.lang.reflect.Method;

/**
 * Created by hexiaofeng on 16-5-10.
 */
public class ValidateTest {

    @Test
    public void testValidate() {

        String test = "test";
        CharSequence se = (CharSequence) test;
        se.length();

        SyncJob job = new SyncJob();
        job.setThreads(1);
        job.setRatio(2);
        job.setNode("1111");
        job.setConfig(new QueueConfig(1, 1));
        Assert.assertTrue(validate(job));
        job.setNode("");
        Assert.assertFalse(validate(job));
        job.setNode("1111");
        job.setThreads(6);
        Assert.assertFalse(validate(job));
        job.setThreads(1);
        job.setRatio(2);
        job.setNode("1111");
        job.setConfig(new QueueConfig(-1, 1));
        Assert.assertFalse(validate(job));

        try {
            Method method = SyncJob.class.getMethod("update",QueueConfig.class);
            Validators.validate(method, job, new QueueConfig(1, 1));
        } catch (NoSuchMethodException e) {
            Assert.assertTrue(false);
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

    }

    private boolean validate(SyncJob job) {
        boolean success = false;
        try {
            Validators.validate(job);
            success = true;
        } catch (ValidateException e) {
            success = false;
        }
        return success;
    }

    public static class SyncJob {
        @Range(max = 5)
        private int threads;
        @DoubleRange(max = 5)
        private double ratio;
        @Size(max = 20)
        @NotEmpty
        private String node;
        @Valid
        private QueueConfig config;

        public SyncJob() {
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public QueueConfig getConfig() {
            return config;
        }

        public void setConfig(QueueConfig config) {
            this.config = config;
        }

        @Valid
        public void update(@NotNull @Valid QueueConfig config) {

        }
    }

    public static class QueueConfig {
        @Range(min = 1, max = Integer.MAX_VALUE)
        private int size;
        @Range(min = 1, max = Integer.MAX_VALUE)
        private int timeout;

        public QueueConfig(int size, int timeout) {
            this.size = size;
            this.timeout = timeout;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
