package com.jd.journalq.model.domain.grafana;

import java.io.Serializable;

public class GrafanaSearch implements Serializable {

    private String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


}
