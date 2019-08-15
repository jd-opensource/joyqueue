package io.chubao.joyqueue.broker.monitor.model;

/**
 * RetryStatPo
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/18
 */
public class RetryStatPo {

    private long success;
    private long failure;

    public RetryStatPo() {

    }

    public RetryStatPo(long success, long failure) {
        this.success = success;
        this.failure = failure;
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