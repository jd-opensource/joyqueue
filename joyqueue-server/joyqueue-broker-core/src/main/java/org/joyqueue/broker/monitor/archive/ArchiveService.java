/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.monitor.archive;

import com.google.common.base.Strings;
import com.jd.laf.extension.ExtensionManager;
import org.joyqueue.broker.archive.ArchiveConfig;
import org.joyqueue.server.archive.store.api.PointTracer;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author lining11
 * Date: 2020/1/2
 */
public class ArchiveService extends Service {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveService.class);

    private String tracerType = "default";

    private PointTracer tracer;

    private ArchiveConfig config;

    public ArchiveService(ArchiveConfig config) {
        this.config = config;
    }

    public PointTracer getTracer() {
        return tracer;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        tracerType = config.getTracerType();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        Iterator<PointTracer> tracers = ExtensionManager.getOrLoadExtensions(PointTracer.class).iterator();

        while (tracers.hasNext()) {
            tracer = tracers.next();
            if (!Strings.isNullOrEmpty(tracer.type()) && tracer.type().equals(tracerType)) {
                logger.info(String.format("Tracer type %s", tracer.type()));
                break;
            }
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        tracer = null;
    }
}
