package com.jd.journalq.model.domain;

/**
 * 带标签属性的公共模型
 * Created by chenyanying3 on 2018/10/19.
 */
public class LabelBaseModel extends BaseModel {
    private String labels;
    private String labelText;

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

}
