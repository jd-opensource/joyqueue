package com.jd.journalq.registry.provider;

import com.jd.journalq.toolkit.URL;
import com.jd.journalq.toolkit.io.Files;

import java.io.File;

/**
 * HTTP远程请求注册中心
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-12 下午5:03
 */
public class FileProvider implements AddressProvider {

    public static final String DEFAULT_REGISTRY = "registry";
    public static final String USER_HOME = "user.home";
    public static final String VAR_HOME = "$home";
    protected URL url;

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String getAddress() throws Exception {
        if (url == null) {
            throw new IllegalStateException("url is null");
        }
        //获取user.home下的文件
        String home = System.getProperty(USER_HOME);
        File file = null;
        String path = url.getPath();
        if (path != null && !path.isEmpty()) {
            path = path.replace(VAR_HOME, home);
            file = new File(path);
        } else if (home != null) {
            file = new File(home, DEFAULT_REGISTRY);
        }
        if (file != null && file.exists()) {
            return Files.read(file);
        }
        return null;
    }

    @Override
    public String type() {
        return "file";
    }
}
