/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.sql.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.nsr.sql.operator.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * ResultSetHelper
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class ResultSetHelper {

    private static final Logger logger = LoggerFactory.getLogger(ResultSetHelper.class);

    private static final ConcurrentMap<Class<?>, List<FieldEntry>> fieldCache = Maps.newConcurrentMap();

    public static <T> T assembleOnce(Class<T> type, ResultSet resultSet) {
        List<Map<String, String>> rows = resultSet.getRows();
        if (CollectionUtils.isEmpty(rows)) {
            return null;
        }
        return doAssemble(type, rows.get(0));
    }

    public static <T> List<T> assembleList(Class<T> type, ResultSet resultSet) {
        List<Map<String, String>> rows = resultSet.getRows();
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }

        List<T> result = Lists.newArrayListWithCapacity(rows.size());
        for (Map<String, String> row : rows) {
            result.add(doAssemble(type, row));
        }
        return result;
    }

    protected static <T> T doAssemble(Class<T> type, Map<String, String> row) {
        try {
            List<FieldEntry> fields = getFields(type);
            T result = type.newInstance();

            for (FieldEntry entry : fields) {
                String value = row.get(entry.getAlias());
                if (value == null) {
                    value = row.get(entry.getAlias().toUpperCase());
                }
                if (value == null) {
                    continue;
                }
                try {
                    Object convertedValue = convertValue(entry.getType(), value);
                    entry.getField().setAccessible(true);
                    entry.getField().set(result, convertedValue);
                } catch (Exception e) {
                    logger.error("doAssemble exception, field: {}, type: {}, columns: {}", entry.getName(), type, row, e);
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("doAssemble exception, type: {}, columns: {}", type, row, e);
            return null;
        }
    }

    protected static <T> T convertValue(Class<T> type, String value) throws Exception {
        if (type.equals(String.class)) {
            return (T) value;
        } else if (type.equals(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (type.equals(Long.class)) {
            return (T) Long.valueOf(value);
        } else if (type.equals(Short.class)) {
            return (T) Short.valueOf(value);
        } else if (type.equals(Byte.class)) {
            return (T) Byte.valueOf(value);
        } else if (type.equals(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (type.equals(Float.class)) {
            return (T) Float.valueOf(value);
        } else if (type.equals(Double.class)) {
            return (T) Double.valueOf(value);
        } else if (type.equals(Date.class)) {
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                return (T) format.parse(value);
            } catch (Exception e) {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return (T) format.parse(value);
            }
        }
        throw new UnsupportedOperationException(type.getName());
    }

    protected static List<FieldEntry> getFields(Class<?> type) {
        List<FieldEntry> fields = fieldCache.get(type);
        if (fields != null) {
            return fields;
        }

        fields = Lists.newArrayList();
        for (Field field : type.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            FieldEntry entry = new FieldEntry();
            entry.setName(field.getName());
            entry.setAlias(field.getName());
            entry.setType(field.getType());
            entry.setField(field);

            if (column != null) {
                entry.setAlias(column.alias());
            }
            fields.add(entry);
        }
        fieldCache.put(type, fields);
        return fields;
    }

    private static class FieldEntry {
        private String name;
        private String alias;
        private Class<?> type;
        private Field field;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public Field getField() {
            return field;
        }
    }
}