package io.chubao.joyqueue.client.internal.common.compress;

import io.chubao.joyqueue.client.internal.Plugins;
import org.apache.commons.lang3.StringUtils;

/**
 * CompressorManager
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class CompressorManager {

    public static Compressor getCompressor(String type) {
        Compressor compressor;

        if (StringUtils.isBlank(type)) {
            compressor = Plugins.COMPRESSORS.get();
        } else {
            compressor = Plugins.COMPRESSORS.get(type);
            if (compressor == null) {
                throw new IllegalArgumentException(String.format("compressor %s not found.", type));
            }
        }
        return compressor;
    }
}