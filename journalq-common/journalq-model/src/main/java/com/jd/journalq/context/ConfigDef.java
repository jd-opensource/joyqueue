package com.jd.journalq.context;

import com.jd.journalq.exception.JMQConfigException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.jd.journalq.context.ConfigDef.Type.OBJECT;

public class ConfigDef {

    private final Map<String, ConfigKey> configKeys = new HashMap<>();

    public ConfigDef define(String name, Object defaultValue, ConfigDef.Type type) {
        if (configKeys.containsKey(name)) {
            throw new JMQConfigException("Configuration " + name + " is defined twice.");
        }
        configKeys.put(name, new ConfigDef.ConfigKey(name, type, defaultValue));
        return this;
    }

    public void parse(Map<?, ?> props) {
        for (Map.Entry<?, ?> entry : props.entrySet()) {
            if (configKeys.containsKey(entry.getKey())) {
                ConfigKey key = configKeys.get(entry.getKey());
                key.value = parseType(key.name, props.get(key.name), key.type);
            } else {
                configKeys.put(entry.getKey().toString(), new ConfigDef.ConfigKey(entry.getKey().toString(), OBJECT, entry.getValue()));
            }
        }
    }

    public void parse(String key, Object value) {
        if (configKeys.containsKey(key)) {
            ConfigKey config = configKeys.get(key);
            config.value = parseType(config.name, value, config.type);
        } else {
            configKeys.put(key, new ConfigDef.ConfigKey(key, OBJECT, value));
        }
    }

    private Object getConfig(String key) {
        ConfigKey configkey = configKeys.get(key);
        if (null == configkey) {
            throw new JMQConfigException("Configuration " + key + "is not exist");
        }
        return configkey.getValue();
    }

    public int getInt(String key) {
        return Integer.valueOf(getConfig(key).toString());
    }

    public long getLong(String key) {
        return Long.valueOf(getConfig(key).toString());
    }

    public String getString(String key) {
        return getConfig(key).toString();
    }

    public short getShort(String key) {
        return Short.valueOf(getConfig(key).toString()) ;
    }

    public double getDouble(String key) {
        return Double.valueOf(getConfig(key).toString());
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(getConfig(key).toString()) ;
    }

    private Object parseType(String name, Object value, ConfigDef.Type type) {
        try {
            if (value == null){
                return null;
            }
            String trimmed = null;
            if (value instanceof String) {
                trimmed = ((String) value).trim();
            }
            switch (type) {
                case BOOLEAN:
                    if (value instanceof String) {
                        if (trimmed.equalsIgnoreCase("true")) {
                            return true;
                        } else if (trimmed.equalsIgnoreCase("false")) {
                            return false;
                        }else {
                            throw new JMQConfigException(name, value, "Expected value to be either true or false");
                        }
                    } else if (value instanceof Boolean) {
                        return value;
                    }else {
                        throw new JMQConfigException(name, value, "Expected value to be either true or false");
                    }
                case STRING:
                    if (value instanceof String) {
                        return trimmed;
                    }else {
                        throw new JMQConfigException(name, value, "Expected value to be a string, but it was a " + value.getClass().getName());
                    }
                case INT:
                    if (value instanceof Integer) {
                        return (Integer) value;
                    } else if (value instanceof String) {
                        return Integer.parseInt(trimmed);
                    } else {
                        throw new JMQConfigException(name, value, "Expected value to be an number.");
                    }
                case SHORT:
                    if (value instanceof Short) {
                        return (Short) value;
                    } else if (value instanceof String) {
                        return Short.parseShort(trimmed);
                    } else {
                        throw new JMQConfigException(name, value, "Expected value to be an number.");
                    }
                case LONG:
                    if (value instanceof Integer) {
                        return ((Integer) value).longValue();
                    }else if (value instanceof Long) {
                        return (Long) value;
                    }else if (value instanceof String) {
                        return Long.parseLong(trimmed);
                    }else {
                        throw new JMQConfigException(name, value, "Expected value to be an number.");
                    }
                case DOUBLE:
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }else if (value instanceof String) {
                        return Double.parseDouble(trimmed);
                    }else {
                        throw new JMQConfigException(name, value, "Expected value to be an number.");
                    }
                default:
                    throw new IllegalStateException("Unknown type.");
            }
        } catch (NumberFormatException e) {
            throw new JMQConfigException(name, value, "Not a number of type " + type);
        }
    }

    public enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, DOUBLE, OBJECT
    }

    public static class ConfigKey {
        public final String name;
        public final ConfigDef.Type type;
        public final Object defaultValue;
        public Object value;

        public ConfigKey(String name, ConfigDef.Type type, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public Object getValue() {
            return null != value ? value : defaultValue;
        }
    }

    public String toRst() {
        Collection<ConfigKey> configs = configKeys.values();
        StringBuilder b = new StringBuilder();
        for (ConfigDef.ConfigKey def : configs) {
            b.append(def.name).append(" : ").append("type [").append(def.type.toString().toLowerCase(Locale.ROOT)).append("],");
            b.append(" default[").append(def.defaultValue).append("],");
            if (def.value != null) {
                b.append(" value[").append(def.value).append("]");
            }
            b.append("\n");
        }
        return b.toString();
    }
}