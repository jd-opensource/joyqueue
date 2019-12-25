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
package org.joyqueue.client.internal.common.compress;

import org.joyqueue.client.internal.Plugins;
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