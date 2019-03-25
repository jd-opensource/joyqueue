package com.jd.journalq.model.query;

import com.jd.journalq.common.model.QKeyword;

/**
 * 命名空间
 * Created by chenyanying3 on 2018-11-17
 */
public class QNamespace extends QKeyword {
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
