package io.openmessaging.spring.config;

/**
 * Parser for the attribute element.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class KeyValueAttribute {

    private String key;
    private String value;

    public KeyValueAttribute() {

    }

    public KeyValueAttribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}