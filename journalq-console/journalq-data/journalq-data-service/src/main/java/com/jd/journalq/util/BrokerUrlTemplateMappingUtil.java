package com.jd.journalq.util;

import com.jd.journalq.model.exception.NotFoundException;
import com.jd.journalq.toolkit.lang.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Broker url template mapping util
 *      Load properties from broker_url_template_config.properties
 */
public class BrokerUrlTemplateMappingUtil {

    public static final String BASE_URL_TEMPLATE_KEY = "baseUrl";

    protected static Properties urlTemplateMapping;

    public static Properties geturlTemplateMapping() throws IOException {
        if (urlTemplateMapping == null) {
            urlTemplateMapping = new Properties();
            urlTemplateMapping.load(BrokerUrlTemplateMappingUtil.class.getClassLoader().getResourceAsStream("broker_url_config.properties"));
        }
        return urlTemplateMapping;
    }

    public static String getUrlTemplate(String path) throws IOException {
        Preconditions.checkArgument(StringUtils.isNotBlank(path), "path can not be blank while getting url form broker_url_template_config.properties by path");
        return geturlTemplateMapping().getProperty(path);
    }

    public static String getFullUrlTemplate(String path) throws IOException {
        //get base url template
        String baseUrl = geturlTemplateMapping().getProperty(BASE_URL_TEMPLATE_KEY);
        if (StringUtils.isBlank(baseUrl)) {
            throw new NotFoundException(String.format("can not find url template with key %s from broker_url_template_config.properties", BASE_URL_TEMPLATE_KEY));
        }
        //get full url template
        return baseUrl + getUrlTemplate(path);
    }
}
