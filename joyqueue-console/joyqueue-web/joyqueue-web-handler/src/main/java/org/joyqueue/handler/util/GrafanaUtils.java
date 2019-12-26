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
package org.joyqueue.handler.util;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Maps;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.model.domain.grafana.GrafanaConfig;
import org.joyqueue.model.domain.grafana.GrafanaVariable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.joyqueue.model.domain.grafana.GrafanaVariable.DEFAULT_GRAFANA_TARGET_DELIMITER;

/**
 * Grafana configuration utils
 * Created by chenyanying3 on 19-3-3.
 */
public class GrafanaUtils {

    private static final Logger logger = LoggerFactory.getLogger(GrafanaUtils.class);

    private static GrafanaConfig config;
    private static Map<String/**variable name*/, GrafanaVariable/**variable definition*/> variables;
    private static Map<String/**metric target key*/, List<String>/**metric value list*/> metrics;
    private static Map<String/**uid*/, String/**url*/> urls;
    public static final String DELIMITER_REG = "[\\pP\\pZ\\pS]";
    public static final String VARIABLE_SYMBOL = "$";

    private static final Object configMutex = new Object();
    private static final Object variableMutex = new Object();
    private static final Object metricMutex = new Object();
    private static final Object urlMutex = new Object();

    public static GrafanaConfig getConfig() {
        if (config != null) {
            return config;
        }

        synchronized (configMutex) {
            if (config == null) {
                config = load("grafana.xml");
            }
            return config;
        }
    }

    public static Map<String, GrafanaVariable> getVariables() {
        if (variables != null) {
            return variables;
        }

        synchronized (variableMutex) {
            if (variables == null) {
                Map<String, GrafanaVariable> map = Maps.newConcurrentMap();
                getConfig().getVariables().forEach(v -> map.put(v.getName(), v));
                variables = map;
            }
            return variables;
        }
    }

    public static Map<String, List<String>> getMetrics() {
        if (metrics != null) {
            return metrics;
        }

        synchronized (metricMutex) {
            if (metrics == null) {
                if (getConfig() != null) {
                    Map<String, List<String>> map = Maps.newConcurrentMap();
                    getConfig().getDashboards().forEach(d -> {
                        if (d.getMetricVariables() != null) {
                            d.getMetricVariables().stream().forEach(v -> {
                                String delimiter = getDelimiter(v.getTarget());
                                String key = v.getName() + delimiter + d.getUid();
                                map.put(key, v.getMetrics().stream().map(m -> m.getName()).collect(Collectors.toList()));
                            });
                        }
                    });
                    metrics = map;
                }
            }
            return metrics;
        }
    }

    public static Map<String, String> getUrls() {
        if (urls != null) {
            return urls;
        }

        synchronized (urlMutex) {
            if (urls == null) {
                if (getConfig() != null) {
                    urls = Maps.newConcurrentMap();
                    String baseUrl = getConfig().getUrl();
                    if (StringUtils.isBlank(baseUrl)) {
                        throw new ConfigException(ErrorCode.InvalidConfiguration, "can not found url property at grafana.xml");
                    }
                    getConfig().getDashboards().forEach(c -> {
                        if (StringUtils.isBlank(c.getUrl())) {
                            logger.error(String.format("can not found path property of dashboard with name %s at grafana.xml", c.getTitle()));
                            return;
                        }
                        if (StringUtils.isBlank(c.getUid())) {
                            logger.error(String.format("can not found uid property of dashboard with name %s at grafana.xml", c.getTitle()));
                            return;
                        }
                        urls.put(c.getUid(), baseUrl + c.getUrl());
                    });
                }
            }
            return urls;
        }
    }

    private static GrafanaConfig load(String file) {
        try {
            logger.info("loading grafana.xml");
            return new XmlMapper().readValue(
                    StringUtils.toEncodedString(IOUtils.toByteArray(GrafanaUtils.class.getClassLoader().getResourceAsStream(file)),
                            StandardCharsets.UTF_8), GrafanaConfig.class);
        } catch (IOException e) {
            throw new ConfigException(ErrorCode.ConfigurationNotExists, "load file grafana.xml error.");
        }
    }

    public static String getDelimiter(String target) {
        // find first symbol as delimiter
        Matcher matcher = Pattern.compile(DELIMITER_REG).matcher(target);
        String delimiter = DEFAULT_GRAFANA_TARGET_DELIMITER;
        if (matcher.find()) {
            delimiter = matcher.group(0);
        }
        return delimiter;
    }

    public static String[] getKey(String target) {
        // find first symbol as delimiter
        Matcher matcher = Pattern.compile(DELIMITER_REG).matcher(target);
        String delimiter = DEFAULT_GRAFANA_TARGET_DELIMITER;
        if (matcher.find()) {
            delimiter = matcher.group(0);
        }
        // join target key
        String[] fields;
        try {
            fields = target.split(delimiter);
        } catch (Exception e) {
            fields = target.split("\\" + delimiter);
        }
        return new String[]{fields[0], fields[0] + delimiter + fields[1]};
    }

    public static String getResult(Object obj, String format) {
        Pattern pattern = Pattern.compile(DELIMITER_REG);
        return Arrays.stream(format.split("\\" + VARIABLE_SYMBOL)).filter(v -> StringUtils.isNotBlank(v)).map(v -> {
            Matcher matcher = pattern.matcher(v);
            if (matcher.find()) {
                return v.substring(0, matcher.end() - 1);
            }
            return v;
        }).distinct().reduce(format, (r, v) -> {
            Class clazz = obj.getClass();
            Field field;
            Object value;
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(v);
                    field.setAccessible(true);
                    value = field.get(obj);
                    return r.replace(VARIABLE_SYMBOL + v, value == null ? "" : value.toString());
                } catch (NoSuchFieldException e) {
                    //find filed form super class
                    clazz = clazz.getSuperclass();
                } catch (Exception e) {
                    logger.error(String.format("get property %s of class %s error.", v, obj.getClass()), e);
                    return r;
                }
            }
            return r;
        });
    }

    public static String getMetricCode(String uid, String name, String granularity) {
        StringBuffer result = new StringBuffer("");
        getConfig().getDashboards().stream().filter(c ->
                c.getUid().equals(uid)).findAny().ifPresent(c -> {
            if (c.getMetricVariables() != null) {
                c.getMetricVariables().stream().forEach(v ->
                        v.getMetrics().stream().filter(m ->
                                m.getName().equals(name)).findAny().ifPresent(m ->
                                m.getGranularities().stream().filter(g ->
                                        g.getName().equals(granularity)).findAny().ifPresent(g ->
                                        result.append(g.getCode()).append(","))));
            }
        });
        return result.length()==0? null:result.deleteCharAt(result.length()-1).toString();
    }

    public static void main(String[] args) {
//        Broker broker = new Broker();
//        broker.setIp("127.0.0.1");
//        broker.setPort(80);
//        broker.setId(12343434);
//        getResult(broker, "$ip:$port$port[$id]");
        load("grafana.xml");
        getKey("metrics:uid:$granularity:*");
    }

}
