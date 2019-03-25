package com.jd.journalq.broker.consumer;

import com.jd.journalq.toolkit.config.Property;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * @author chengzhiliang on 2018/10/22.
 */
public class ConsumeConfig {
    private static final String CONSUME_POSITION_PATH = "/position";
    private PropertySupplier propertySupplier;
    private String consumePositionPath;

    public ConsumeConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getConsumePositionPath() {

        if (consumePositionPath == null || consumePositionPath.isEmpty()) {
            synchronized (this) {
                if (consumePositionPath == null) {
                    String prefix = "";
                    if (propertySupplier != null) {
                        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
                        prefix = property == null ? prefix : property.getString();
                    }
                    consumePositionPath = prefix + CONSUME_POSITION_PATH;
                }

            }
        }
        return consumePositionPath;
    }

    public void setConsumePositionPath(String consumePositionPath) {
        this.consumePositionPath = consumePositionPath;
    }
}
