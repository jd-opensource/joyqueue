package org.joyqueue.broker.joyqueue0.command;

import com.google.common.base.Preconditions;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Payload;
import org.joyqueue.network.session.ConnectionId;

/**
 * 删除连接
 */
public class RemoveConnection extends Joyqueue0Payload {
    // 连接ID
    private ConnectionId connectionId;

    public RemoveConnection() {
    }

    public RemoveConnection connectionId(final ConnectionId connectionId) {
        setConnectionId(connectionId);
        return this;
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(connectionId != null, "connectionId can not be null.");
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.REMOVE_CONNECTION.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RemoveConnection{");
        sb.append("connectionId=").append(connectionId);
        sb.append('}');
        return sb.toString();
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

        RemoveConnection that = (RemoveConnection) o;

        if (connectionId != null ? !connectionId.equals(that.connectionId) : that.connectionId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (connectionId != null ? connectionId.hashCode() : 0);
        return result;
    }
}