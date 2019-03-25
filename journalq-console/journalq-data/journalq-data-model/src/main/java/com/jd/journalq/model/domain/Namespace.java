package com.jd.journalq.model.domain;

import com.jd.journalq.model.domain.nsr.BaseNsrModel;

/**
 * 命名空间
 * Created by chenyanying3 on 2018-11-17
 */
public class Namespace extends BaseNsrModel {
//    public static final Identity DEFAULT_NAMESPACE_IDENTITY = new Identity(0l,"");
    public static final String DEFAULT_NAMESPACE_ID = "";
    public static final String DEFAULT_NAMESPACE_CODE = "";

    private String code = DEFAULT_NAMESPACE_CODE;
    private String name;

    public Namespace() {
    }

    public Namespace(String code) {
        this.code = code;
        this.id = code;
    }

    public Namespace(String id, String code) {
        this.id = id;
        this.code = code;
    }

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
