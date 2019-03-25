package com.jd.journalq.nsr;


import com.jd.journalq.nsr.network.NsrCommandHandler;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.util.DCMatcher;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;

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



}
