package org.joyqueue.broker.joyqueue0.command;

import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;
import org.joyqueue.network.session.TransactionId;

/**
 * 事务命令
 */
public abstract class Transaction extends Joyqueue0Payload {
    // 事务ID
    protected TransactionId transactionId;
    //主题
    protected String topic;

    protected Transaction() {
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Transaction that = (Transaction) o;

        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        return result;
    }
}
