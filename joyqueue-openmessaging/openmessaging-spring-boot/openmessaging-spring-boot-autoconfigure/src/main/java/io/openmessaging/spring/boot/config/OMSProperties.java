package io.openmessaging.spring.boot.config;

import io.openmessaging.spring.boot.OMSSpringBootConsts;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for OpenMessaging.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
@ConfigurationProperties(prefix = OMSSpringBootConsts.PREFIX)
public class OMSProperties {

    private String url;
    private Map<String, String> attributes;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}