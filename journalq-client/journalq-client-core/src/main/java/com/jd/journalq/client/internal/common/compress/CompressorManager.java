package com.jd.journalq.client.internal.common.compress;

import com.jd.journalq.client.internal.Plugins;
import com.jd.journalq.toolkit.lang.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * CompressorManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/2
 */
public class CompressorManager {

    public static Compressor getCompressor(String type) {
        Compressor compressor;

        if (StringUtils.isBlank(type)) {
            compressor = Plugins.COMPRESSORS.get();
        } else {
            compressor = Plugins.COMPRESSORS.get(type);
        }

        Preconditions.checkArgument(compressor != null, String.format("compressor %s not found.", type));
        return compressor;
    }
}