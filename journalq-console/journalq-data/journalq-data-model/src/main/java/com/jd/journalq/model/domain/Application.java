package com.jd.journalq.model.domain;

import javax.validation.constraints.Pattern;

/**
 * Created by yangyang115 on 18-7-26.
 */

public class Application extends BaseModel implements Identifier, Cloneable {

    public static final int JONE_SOURCE = 1;
    public static final int JDOS_SOURCE = 2;
    public static final int SURE_SOURCE = 3;//金融
    public static final int OTHER_SOURCE = 0;

    //应用代码
    @Pattern(regexp = "^[a-zA-Z]+[a-zA-Z0-9_-]*[a-zA-Z0-9]+$", message = "Please enter correct code")
    private String code;
    //别名或英文名
    private String aliasCode;
    //名称
    private String name;
    //系统
    private String system;
    //部门
    private String department;
    //来源
    private int source;
    //签名
    private int sign;
    //所有者
    private Identity owner;
    private String erp;
    private String discription;

    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }

    public Application() {

    }

    public Application(long id) {
        this.id = id;
    }

    public Application(Identity app) {
        this.id = app.getId();
        this.code = app.getCode();
        this.name = app.getName();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAliasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public Identity getOwner() {
        return owner;
    }

    public void setOwner(Identity owner) {
        this.owner = owner;
    }

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    /**
     * 判断是否是所有者
     *
     * @param user
     * @return
     */
    public boolean isOwner(final User user) {
        if (user == null || owner == null || owner.getId() != user.getId()) {
            return false;
        }
        return true;
    }

    @Override
    public Application clone() {
        try {
            return (Application) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public enum SourceType {

        JONE(JONE_SOURCE, "JONE"),
        JDOS(JDOS_SOURCE, "JDOS"),
        OTHER(OTHER_SOURCE, "其他录入");

        private int value;
        private String description;

        SourceType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        public String description() {
            return description;
        }

        public static SourceType valueOf(int value) {
            switch (value) {
                case 0:
                    return OTHER;
                case 1:
                    return JONE;
                case 2:
                    return JDOS;
                default:
                    return OTHER;
            }
        }
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", system='" + system + '\'' +
                ", department='" + department + '\'' +
                ", source=" + source +
                ", sign=" + sign +
                ", owner=" + owner +
                '}';
    }
}
