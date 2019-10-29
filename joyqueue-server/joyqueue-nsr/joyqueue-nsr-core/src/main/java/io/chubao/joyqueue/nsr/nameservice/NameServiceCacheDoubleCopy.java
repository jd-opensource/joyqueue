package io.chubao.joyqueue.nsr.nameservice;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.toolkit.io.DoubleCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class NameServiceCacheDoubleCopy extends DoubleCopy {

    protected static final Logger logger = LoggerFactory.getLogger(NameServiceCacheDoubleCopy.class);

    private static final int VERSION_V0 = 0;
    private static final int CURRENT_VERSION = VERSION_V0;

    private File file;

    private NameServiceCacheEntry entry;

    public NameServiceCacheDoubleCopy(File file) throws IOException {
        super(file, Integer.MAX_VALUE);
        this.file = file;
    }

    public void flush(NameServiceCache cache) {
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
        byte[] json = JSON.toJSONBytes(entry);

        if (logger.isDebugEnabled()) {
            logger.debug("save nameservice cache, value: {}, file: {}", new String(json), file);
        }

        return json;
    }

    @Override
    protected void parse(byte[] data) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("load nameservice cache, value: {}, file: {}", new String(data), file);
            }

            NameServiceCacheEntry entry = JSON.parseObject(data, NameServiceCacheEntry.class);

            if (entry.getVersion() != CURRENT_VERSION) {
                logger.warn("nameservice cache check version failed, current: {}, required: {}", entry.getVersion(), CURRENT_VERSION);
                throw new NsrException("check version failed");
            }

            this.entry = entry;
        } catch (Exception e) {
            logger.error("load nameservice cache exception, file: {}", file, e);
            if (e instanceof NsrException) {
                throw e;
            } else {
                throw new NsrException(e);
            }
        }
    }

    public NameServiceCache getCache() {
        if (entry == null) {
            return null;
        }
        return entry.getCache();
    }

    public static class NameServiceCacheEntry {
        private int version;
        private NameServiceCache cache;

        NameServiceCacheEntry() {

        }

        NameServiceCacheEntry(int version, NameServiceCache cache) {
            this.version = version;
            this.cache = cache;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public NameServiceCache getCache() {
            return cache;
        }

        public void setCache(NameServiceCache cache) {
            this.cache = cache;
        }
    }
}