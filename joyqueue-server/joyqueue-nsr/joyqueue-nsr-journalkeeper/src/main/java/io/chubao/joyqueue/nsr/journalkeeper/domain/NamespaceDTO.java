package io.chubao.joyqueue.nsr.journalkeeper.domain;

/**
 * NamespaceDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class NamespaceDTO extends BaseDTO {

    private String id;
    private String code;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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