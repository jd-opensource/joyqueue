package com.jd.journalq.handler.util;

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
		long now = System.currentTimeMillis();
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
