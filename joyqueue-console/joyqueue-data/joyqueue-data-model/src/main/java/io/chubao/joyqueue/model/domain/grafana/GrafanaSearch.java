package io.chubao.joyqueue.model.domain.grafana;

import java.io.Serializable;

/**
 * Grafana search variable model
 * author: chenyanying3
 * email: chenyanying3@jd.com
 * date: 2019/02/29
 */
public class GrafanaSearch implements Serializable {

    private String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


}
