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
package org.joyqueue.handler.util;

import org.joyqueue.toolkit.time.SystemClock;

import java.util.Date;

public class RetryUtils {
	// 过期时间
	public static final long EXPIRETIME = 1000 * 3600 * 24 * 7;
	// 重试延迟（默认1秒）
	private static final long RETRYDELAY = 1000L;
	// 最大重试延迟（默认5分钟）
	private static final long MAXRETRYDELAY = 1000 * 60 * 5;
	// 指数
	private static final double EXPONENTIAL = 3.0;

	/**
	 * 获取过期时间
	 * 
	 * @return
	 */
	public static Date getExpireTime() {
		long now = SystemClock.now();
		long time = now + EXPIRETIME;
		return new Date(time);
	}

	/**
	 * 获取下一次重试时间
	 * 
	 * @param now
	 * @param retryCount
	 * @return
	 */
	public static Date getNextRetryTime(Date now, int retryCount) {
		if (now == null)
			now = new Date();
		long time = Math.round(RETRYDELAY
				* Math.pow(EXPONENTIAL, retryCount + 1));
		if (time <= 0)
			return now;
		if (time > MAXRETRYDELAY) {
			time = Math.max(MAXRETRYDELAY, RETRYDELAY);
		}
		return new Date(now.getTime() + time);
	}
}
