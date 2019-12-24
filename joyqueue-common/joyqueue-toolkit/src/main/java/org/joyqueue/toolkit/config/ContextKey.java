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

/**
 * 构造上下文中的键
 * Created by hexiaofeng on 16-5-11.
 */
public interface ContextKey {
    String JOB_SUFFIX = "Job";
    String SERVICE_SUFFIX = "Service";
    String INTERVAL_SUFFIX = ".interval";
    String CRON_SUFFIX = ".cron";

    /**
     * 获取Key
     *
     * @return 时间Key
     */
    String getKey();

    /**
     * 配置的键
     */
    class ConfigKey implements ContextKey {

        protected String key;

        public ConfigKey() {
        }

        public ConfigKey(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    /**
     * 任务键生成器
     */
    class JobKey extends ConfigKey {
        public JobKey(String className, String suffix) {
            if (className == null || className.isEmpty()) {
                throw new IllegalArgumentException("className can not be empty.");
            }
            if (className.endsWith(JOB_SUFFIX)) {
                className = className.substring(0, className.length() - JOB_SUFFIX.length());
            } else if (className.endsWith(SERVICE_SUFFIX)) {
                className = className.substring(0, className.length() - SERVICE_SUFFIX.length());
            }
            int count = 0;
            StringBuilder builder = new StringBuilder();
            char[] chars = className.toCharArray();
            for (char ch : chars) {
                count++;
                if (Character.isUpperCase(ch)) {
                    if (count > 1) {
                        builder.append('.');
                    }
                    builder.append(Character.toLowerCase(ch));
                } else {
                    builder.append(ch);
                }
            }
            if (suffix != null) {
                builder.append(suffix);
            }
            key = builder.toString();
        }
    }

    /**
     * 定时间隔任务键生成器
     */
    class IntervalJobKey extends JobKey {
        public IntervalJobKey(String name) {
            super(name, INTERVAL_SUFFIX);
        }
    }

    /**
     * 定时任务键生成器
     */
    class CronJobKey extends JobKey {
        public CronJobKey(String name) {
            super(name, CRON_SUFFIX);
        }
    }
}
