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
package org.joyqueue.toolkit.config;

import org.joyqueue.toolkit.config.BinderListener;
import org.joyqueue.toolkit.config.Binders;
import org.joyqueue.toolkit.config.Context;
import org.joyqueue.toolkit.config.PostmanUpdater;
import org.joyqueue.toolkit.config.annotation.BooleanBinding;
import org.joyqueue.toolkit.config.annotation.DateBinding;
import org.joyqueue.toolkit.config.annotation.DoubleBinding;
import org.joyqueue.toolkit.config.annotation.NumberBinding;
import org.joyqueue.toolkit.config.annotation.StringBinding;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class ContextTest {

    @Test
    public void testBind() throws Exception {
        final Date now = new Date();
        PostmanUpdater postman = new PostmanUpdater() {
            @Override
            protected Context update(final String group) {
                Context context = new Context();
                context.put("threads", 2);
                context.put("ratio", 2.0);
                context.put("overWrite", false);
                context.put("node", "yyyy");
                context.put("startTime", now);
                return context;
            }
        };
        postman.start();
        try {
            Context context = postman.get("sync");
            Assert.assertNotNull(context);
            SyncJob job = new SyncJob();
            Binders.bind(context, job);
            Assert.assertEquals(job.getThreads(), 2);
            Assert.assertEquals(job.getRatio(), 2.0, 0);
            Assert.assertEquals(job.isOverWrite(), false);
            Assert.assertEquals(job.getNode(), "yyyy");
            Assert.assertEquals(job.getStartTime(), now);

            job = new SyncJob();
            postman.addListener("sync", new BinderListener(job));
            Assert.assertEquals(job.getThreads(), 2);
            Assert.assertEquals(job.getRatio(), 2.0, 0);
        } finally {
            postman.stop();
        }
    }

    public static class SyncJob {
        @NumberBinding(key = "threads", defaultValue = 1)
        private int threads;
        @DoubleBinding(key = "ratio", defaultValue = 1.0)
        private double ratio;
        @BooleanBinding(key = "overWrite", defaultValue = true)
        private boolean overWrite;
        @StringBinding(key = "node", defaultValue = "xxx")
        private String node;
        @DateBinding(key = "startTime", defaultValue = "1970-01-01 00:00:00")
        private Date startTime;

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

        public boolean isOverWrite() {
            return overWrite;
        }

        public void setOverWrite(boolean overWrite) {
            this.overWrite = overWrite;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }
    }
}
