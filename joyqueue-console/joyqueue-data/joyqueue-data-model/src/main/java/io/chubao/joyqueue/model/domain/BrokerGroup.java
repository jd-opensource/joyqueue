package io.chubao.joyqueue.model.domain;

/**
 * Created by  cyy on 16-9-19.
 */
public class BrokerGroup extends LabelBaseModel {

    private String code;
    private String name;
    private String description;

    public BrokerGroup() {
    }

    public BrokerGroup(String code) {
        this.code = code;
    }

    public BrokerGroup(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
