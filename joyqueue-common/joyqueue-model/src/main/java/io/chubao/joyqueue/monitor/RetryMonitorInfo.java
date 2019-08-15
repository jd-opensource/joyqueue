package io.chubao.joyqueue.monitor;

/**
 * 重试信息
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class RetryMonitorInfo extends BaseMonitorInfo {

    private long count;
    private long success;
    private long failure;

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFailure() {
        return failure;
    }

    public void setFailure(long failure) {
        this.failure = failure;
    }
}