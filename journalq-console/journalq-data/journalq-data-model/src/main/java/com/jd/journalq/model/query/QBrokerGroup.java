package com.jd.journalq.model.query;

import com.jd.journalq.model.QKeyword;
import com.jd.journalq.model.Query;

/**
 * Created by lining on 17-7-17.
 */
public class QBrokerGroup extends QKeyword implements Query {
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
