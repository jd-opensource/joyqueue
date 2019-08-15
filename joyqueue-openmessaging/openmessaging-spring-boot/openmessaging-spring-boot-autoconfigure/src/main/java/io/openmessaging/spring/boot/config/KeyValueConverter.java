package io.openmessaging.spring.boot.config;

import io.openmessaging.KeyValue;
import io.openmessaging.OMS;

import java.util.Map;

/**
 * Convert attributes to KeyValue.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class KeyValueConverter {

    public static KeyValue convert(Map<String, String> attributes) {
        KeyValue result = OMS.newKeyValue();
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
