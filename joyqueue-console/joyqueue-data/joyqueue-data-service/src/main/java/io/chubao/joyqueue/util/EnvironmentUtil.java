package io.chubao.joyqueue.util;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * EnvironmentUtils
 * author: gaohaoxiang
 * date: 2019/11/8
 */
@Component
public class EnvironmentUtil implements EnvironmentAware {

    private static final String DEV = "dev";
    private static final String TEST = "test";
    private static final String PROD = "prod";

    private static Environment environment;

    public static boolean isDev() {
        return isCurrentEnv(DEV);
    }

    public static boolean isTest() {
        return isCurrentEnv(TEST);
    }

    public static boolean isProd() {
        return isCurrentEnv(PROD);
    }

    public static boolean isCurrentEnv(String env) {
        return ArrayUtils.contains(environment.getActiveProfiles(), env);
    }

    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentUtil.environment = environment;
    }
}