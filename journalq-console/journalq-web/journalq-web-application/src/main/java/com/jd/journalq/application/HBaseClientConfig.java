package com.jd.journalq.application;

import com.jd.journalq.server.archive.store.api.ArchiveStore;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangxiaofei1 on 2018/12/19.
 */
@Configuration
public class HBaseClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(HBaseClientConfig.class);
    private ExtensionPoint<ArchiveStore, String> archiveStores = new ExtensionPointLazy<>(ArchiveStore.class);

    @Bean(value="archiveStore", destroyMethod="stop")
    public ArchiveStore getArchiveStore(){
        ArchiveStore archiveStore =  archiveStores.get();
        if (archiveStore != null) {
            try {
                archiveStore.start();
            } catch (Exception e) {
                logger.error(" archiveStore.start error",e);
            }
        }
        return archiveStore;
    }

}
