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
package com.jd.journalq.toolkit.service;

import com.jd.journalq.toolkit.lang.Pair;
import com.jd.journalq.toolkit.time.CronExpression;
import com.jd.journalq.toolkit.config.Context;
import com.jd.journalq.toolkit.config.ContextKey;
import com.jd.journalq.toolkit.config.Postman;

import java.util.Date;

/**
 * 时钟服务，定时获取上下文配置并执行
 * Created by hexiaofeng on 15-3-12.
 */
public abstract class WorkerService extends Service {
    public static final int INTERVAL = 5000;
    // 上下文的键
    protected WorkerSchedule schedule;
    // 上下文构建器
    protected Postman postman;
    // 配置分组
    protected String group;
    // 是否要做选举
    protected Election election;
    // 工作线程
    protected Thread worker;

    public WorkerService() {
    }

    public WorkerService(long interval) {
        this(interval, interval, null);
    }

    public WorkerService(long interval, long initialDelay) {
        this(interval, initialDelay, null);
    }

    public WorkerService(long interval, ContextKey key) {
        this(interval, interval, null);
    }

    public WorkerService(long interval, long initialDelay, ContextKey key) {
        if (key == null) {
            key = new ContextKey.IntervalJobKey(this.getClass().getName());
        }
        schedule = new FixSchedule(interval, initialDelay, key);
    }

    public WorkerService(String cron, ContextKey key) {
        if (key == null) {
            key = new ContextKey.CronJobKey(this.getClass().getName());
        }
        schedule = new CronSchedule(cron, key);
    }

    public void setSchedule(WorkerSchedule schedule) {
        this.schedule = schedule;
    }

    public void setPostman(Postman postman) {
        this.postman = postman;
    }

    @Override
    protected void validate() throws Exception {
        if (schedule == null) {
            schedule = new FixSchedule(INTERVAL, INTERVAL, new ContextKey.IntervalJobKey(this.getClass().getName()));
        }
        if (group == null || group.isEmpty()) {
            group = new ContextKey.JobKey(this.getClass().getSimpleName(), null).getKey();
        }
        super.validate();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        // 是否要做选举
        if (election != null) {
            election.register(new ElectionListener() {
                @Override
                public void onTake() {
                    startWorker();
                }

                @Override
                public void onLost() {
                    stopWorker();
                }
            });
        } else {
            startWorker();
        }
    }

    @Override
    protected void doStop() {
        if (election != null) {
            election.deregister();
        }
        stopWorker();
        super.doStop();
    }

    /**
     * 启动任务
     */
    protected void startWorker() {
        // 创建定时工作线程
        worker = new Thread(new Worker(schedule.getInitialDelay()), this.getClass().getSimpleName());
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * 停止服务
     */
    protected void stopWorker() {
        if (worker != null && !worker.isInterrupted()) {
            worker.interrupt();
        }
    }

    /**
     * 出现异常
     *
     * @param e 异常
     * @return true 如果继续调度
     */
    protected boolean onException(final Throwable e) {
        return true;
    }

    /**
     * 执行任务
     *
     * @param context 上下文
     * @throws Exception
     */
    protected abstract void execute(final Context context) throws Exception;

    /**
     * 执行器
     */
    protected class Worker extends ServiceThread {
        protected boolean initialDelay;

        public Worker(long interval) {
            super(WorkerService.this, interval);
            // 表示需要初始化调度时间
            this.initialDelay = interval >= 0;
        }

        @Override
        public boolean isStarted() {
            return super.isStarted() && (election == null || election.isLeader());
        }

        @Override
        protected void execute() throws Exception {
            // 构建上下文
            Context context = postman.get(group);
            if (!initialDelay) {
                // 必须确保第一次拿到时间才进行
                interval = schedule.getInterval(context);
                initialDelay = true;
            } else {
                try {
                    // 执行
                    WorkerService.this.execute(context);
                } finally {
                    // 下次执行时间
                    this.interval = schedule.getInterval(context);
                }
            }
        }

        @Override
        public boolean onException(Throwable e) {
            return WorkerService.this.onException(e);
        }

    }

    /**
     * 固定时间间隔
     */
    public static class FixSchedule implements WorkerSchedule {
        protected long interval;
        protected long initialDelay;
        protected ContextKey key;

        public FixSchedule() {
            this(INTERVAL, INTERVAL, null);
        }

        public FixSchedule(long interval) {
            this(interval, interval, null);
        }

        public FixSchedule(long interval, ContextKey key) {
            this(interval, interval, key);
        }

        public FixSchedule(long interval, long initialDelay, ContextKey key) {
            this.interval = interval < 0 ? 0 : interval;
            this.initialDelay = initialDelay < 0 ? 0 : initialDelay;
            this.key = key;
        }

        @Override
        public long getInterval(final Context context) throws Exception {
            if (context == null || key == null) {
                return interval;
            }
            return context.getLong(key.getKey(), interval);
        }

        @Override
        public long getInitialDelay() {
            return initialDelay;
        }
    }

    /**
     * 按照调度计划
     */
    public static class CronSchedule implements WorkerSchedule {
        protected String cron;
        protected Pair<String, CronExpression> pair;
        protected ContextKey key;

        public CronSchedule() {
        }

        public CronSchedule(String cron) {
            this.cron = cron;
        }

        public CronSchedule(String cron, ContextKey key) {
            this.cron = cron;
            this.key = key;
        }

        @Override
        public long getInterval(final Context context) throws Exception {
            String value = key == null ? cron : context.getString(key.getKey(), cron);
            // 获取表达式字符串
            if (value != null && !value.isEmpty()) {
                if (pair == null || !value.equals(pair.getKey())) {
                    pair = new Pair<String, CronExpression>(value, new CronExpression(value));
                }
                // 获取下一个时间间隔
                Date now = new Date();
                Date next = pair.getValue().getNextValidTimeAfter(now);
                return next.getTime() - now.getTime();
            }
            return INTERVAL;
        }

        @Override
        public long getInitialDelay() {
            return 0;
        }
    }

}
