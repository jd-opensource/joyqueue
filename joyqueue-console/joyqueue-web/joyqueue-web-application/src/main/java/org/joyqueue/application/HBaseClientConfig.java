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
package org.joyqueue.application;

import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import org.joyqueue.server.archive.store.api.ArchiveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangxiaofei1 on 2018/12/19.
 */
@Configuration
@ConditionalOnProperty(name = "archive.enable", havingValue = "true")
public class HBaseClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(HBaseClientConfig.class);
    private ExtensionPoint<ArchiveStore, String> archiveStores = new ExtensionPointLazy<>(ArchiveStore.class);

    @Value("${hbase.namespace:joyqueue}")
    private String namespace;

    @Bean(value="archiveStore", destroyMethod="stop")
    public ArchiveStore getArchiveStore(){
        ArchiveStore archiveStore =  archiveStores.get();
        archiveStore.setNameSpace(namespace);
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
