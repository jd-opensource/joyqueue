package io.chubao.joyqueue.model.domain;

/**
 * Broker实例
 *
 * @author tianya
 */
public class BrokerGroupRelated extends BaseModel {
    /**
     * 分组
     */
    private Identity group;

    public Identity getGroup() {
        return group;
    }

    public void setGroup(Identity group) {
        this.group = group;
    }
}