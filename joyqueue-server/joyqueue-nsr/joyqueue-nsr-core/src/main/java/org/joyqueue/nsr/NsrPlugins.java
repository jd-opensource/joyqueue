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
package org.joyqueue.nsr;


import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.nsr.network.NsrCommandHandler;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.util.DCMatcher;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public interface NsrPlugins {
    /**
     * nameserver payloadCodec extensionPoint
     */
    ExtensionPoint<NsrPayloadCodec, String> nsrPayloadCodecPlugins = new ExtensionPointLazy<>(NsrPayloadCodec.class, SpiLoader.INSTANCE, null, null);
    /**
     * nameserver nameServiceCommandHandler extensionPoint
     */
    ExtensionPoint<NsrCommandHandler, String> nsrCommandHandlerPlugins = new ExtensionPointLazy<>(NsrCommandHandler.class, SpiLoader.INSTANCE, null, null);
    /**
     * DCMatcher extensionPoint
     */
    ExtensionPoint<DCMatcher, String> DCMatchersPlugins = new ExtensionPointLazy<>(DCMatcher.class, SpiLoader.INSTANCE, null, null);
    /**
     * 跟踪器服务扩展点
     */
    ExtensionPoint<PointTracer, String> TRACERERVICE = new ExtensionPointLazy<>(PointTracer.class, SpiLoader.INSTANCE, null, null);


}
