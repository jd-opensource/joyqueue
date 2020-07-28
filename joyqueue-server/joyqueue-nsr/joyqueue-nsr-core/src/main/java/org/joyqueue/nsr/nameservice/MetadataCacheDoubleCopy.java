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
package org.joyqueue.nsr.nameservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.toolkit.io.DoubleCopy;
import org.joyqueue.toolkit.io.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MetadataCacheDoubleCopy extends DoubleCopy {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataCacheDoubleCopy.class);

    private static final int DEFAULT_MAX_LENGTH =  1024 * 1024 * 100;

    private static final int VERSION_V0 = 0;
    private static final int CURRENT_VERSION = VERSION_V0;

    private File file;

    protected NameServiceCacheEntry entry;

    public MetadataCacheDoubleCopy(File file, int maxLength) throws IOException {
        super(file, maxLength);
        this.file = file;
    }

    public MetadataCacheDoubleCopy(File file) throws IOException {
        this(file, DEFAULT_MAX_LENGTH);
    }

    public void flush(AllMetadataCache cache) {
        if (entry == null) {
            entry = new NameServiceCacheEntry(CURRENT_VERSION, cache);
        } else {
            entry = new NameServiceCacheEntry(entry.getVersion(), cache);
        }
        super.flush();
    }

    @Override
    protected String getName() {
        return "NameServiceCache";
    }

    @Override
    protected byte[] serialize() {
        try {
            byte[] json = JSON.toJSONBytes(entry, SerializerFeature.DisableCircularReferenceDetect);
            json = ZipUtil.compress(json);

            if (logger.isDebugEnabled()) {
                logger.debug("save metadata cache, value: {}, file: {}", new String(json), file);
            }

            return json;
        } catch (Exception e) {
            logger.error("serialize cache exception", e);
            return new byte[0];
        }
    }

    @Override
    protected void parse(byte[] data) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("load metadata cache, value: {}, file: {}", new String(data), file);
            }

            data = ZipUtil.decompress(data).getBytes();
            NameServiceCacheEntry entry = JSON.parseObject(data, NameServiceCacheEntry.class);

            if (entry != null && entry.getVersion() != CURRENT_VERSION) {
                logger.warn("metadata cache check version failed, current: {}, required: {}", entry.getVersion(), CURRENT_VERSION);
                throw new NsrException("check version failed");
            }

            this.entry = entry;
        } catch (Exception e) {
            logger.error("load metadata cache exception, file: {}", file, e);
            if (e instanceof NsrException) {
                throw (NsrException) e;
            } else {
                throw new NsrException(e);
            }
        }
    }

    public AllMetadataCache getCache() {
        if (entry == null) {
            return null;
        }
        return entry.getCache();
    }

    public static class NameServiceCacheEntry {
        private int version;
        private AllMetadataCache cache;

        NameServiceCacheEntry() {

        }

        NameServiceCacheEntry(int version, AllMetadataCache cache) {
            this.version = version;
            this.cache = cache;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public AllMetadataCache getCache() {
            return cache;
        }

        public void setCache(AllMetadataCache cache) {
            this.cache = cache;
        }
    }
}