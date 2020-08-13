package org.joyqueue.nsr.sql.operator.support;

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.nsr.sql.config.SQLConfigKey;
import org.joyqueue.nsr.sql.operator.DataSourceFactory;
import org.joyqueue.toolkit.util.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * DefaultDataSourceFactory
 * author: gaohaoxiang
 * date: 2020/8/13
 */
public class DefaultDataSourceFactory implements DataSourceFactory {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultDataSourceFactory.class);

    @Override
    public DataSource createDataSource(Properties properties) {
        String dataSourceClassName = properties.getProperty(SQLConfigKey.DATASOURCE_CLASS.getName());
        if (StringUtils.isBlank(dataSourceClassName)) {
            throw new IllegalArgumentException("datasource class not exist");
        }
        Class<?> dataSourceClass = null;
        try {
            dataSourceClass = Class.forName(dataSourceClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("datasource class not exist, class: %s", dataSourceClassName));
        }

        try {
            Object dataSource = dataSourceClass.newInstance();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (String.valueOf(entry.getKey()).equals(SQLConfigKey.DATASOURCE_CLASS.getName())) {
                    continue;
                }

                String key = String.valueOf(entry.getKey()).substring(SQLConfigKey.DATASOURCE_PROPERTIES_PREFIX.getName().length());
                Method setterMethod = findSetterMethod(dataSourceClass, key);
                if (setterMethod == null) {
                    logger.warn("not found {} field", key);
                } else {
                    setterMethod.invoke(dataSource, ConvertUtils.convert(entry.getValue(), setterMethod.getParameterTypes()[0]));
                    logger.info("set datasource property {} to {}", key, entry.getValue());
                }
            }
            return (DataSource) dataSource;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected Method findSetterMethod(Class<?> type, String methodName) {
        String setterMethodName = "set" + StringUtils.capitalize(methodName);
        for (Method method : type.getMethods()) {
            if (method.getName().equals(setterMethodName)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public String type() {
        return "default";
    }
}