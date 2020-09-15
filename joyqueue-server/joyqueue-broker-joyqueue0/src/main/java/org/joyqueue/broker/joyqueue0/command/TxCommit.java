package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;

/**
 * TxCommit
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class TxCommit extends Transaction {
    @Override
    public void validate() {
        super.validate();
        if (transactionId == null) {
            throw new IllegalStateException("transaction is null");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Commit{");
        sb.append("transactionId=").append(transactionId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.COMMIT.getCode();
    }
}
