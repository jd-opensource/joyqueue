package io.chubao.joyqueue.tools.config;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * ConsoleConsumerConfig
 *
 * author: gaohaoxiang
 * date: 2019/6/26
 */
public class ConsoleConsumerConfig {

    @Parameter(names = {"-s", "--server"}, description = "server bootstrap")
    private String bootstrap = "localhost:50088";

    @Parameter(names = {"-t", "--topic"}, description = "consumer topic", required = true)
    private String topic;

    @Parameter(names = {"-n", "--namespace"}, description = "consumer namespace")
    private String namespace;

    @Parameter(names = {"-r", "--region"}, description = "consumer region")
    private String region;

    @Parameter(names = {"-a", "--app"}, description = "consumer app", required = true)
    private String app;

    @Parameter(names = {"-T", "--token"}, description = "app token", required = true)
    private String token;

    @DynamicParameter(names = {"-p", "--params"}, description = "consumer params")
    private Map<String, String> params = new HashMap<>();

    @Parameter(names = "--help", description = "help", help = true)
    private boolean help = false;

    public String getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }
}