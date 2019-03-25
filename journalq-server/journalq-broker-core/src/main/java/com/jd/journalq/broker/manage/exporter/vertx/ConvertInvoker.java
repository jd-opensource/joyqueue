package com.jd.journalq.broker.manage.exporter.vertx;

import com.jd.journalq.broker.monitor.converter.Converter;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ConvertInvoker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/21
 */
public class ConvertInvoker extends HandlerInvoker {
    /**
     * 重试扩展点
     */
    ExtensionPoint<Converter, String> converters = new ExtensionPointLazy<>(Converter.class, SpiLoader.INSTANCE, null, null);

    private static final String CONVERTER_PARAM = "_target";

    private static final String CONVERTER_HEADER = "User-Agent";

    protected static final Logger logger = LoggerFactory.getLogger(ConvertInvoker.class);

    public ConvertInvoker(Object service, String methodName, Map<String, Class<?>> params) {
        super(service, methodName, params);
    }

    @Override
    public Object invoke(RoutingContext context) throws Exception {
        String target = getTarget(context);
        Object result = super.invoke(context);

        if (StringUtils.isBlank(target) || result == null) {
            return result;
        }

        Converter converter =converters.get(target);
        if (converter == null) {
            return result;
        }
        return converter.convert(result);
    }

    protected String getTarget(RoutingContext context) {
        String targetParam = context.request().getParam(CONVERTER_PARAM);
        if (StringUtils.isNotBlank(targetParam)) {
            return targetParam;
        }
        return context.request().getHeader(CONVERTER_HEADER);
    }
}