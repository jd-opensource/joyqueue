package io.chubao.joyqueue.model.domain.nsr;

import java.io.Serializable;

/**
 * NSR
 * Created by chenyanying3 on 19-3-3.
 */
public class BaseNsrModel implements Serializable, Cloneable {

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return "BaseNsrModel{" +
                "id=" + id +
                '}';
    }
}
