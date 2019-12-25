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
package org.joyqueue.convert;

import org.joyqueue.model.domain.Namespace;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public class NsrNameSpaceConverter extends Converter<Namespace, org.joyqueue.domain.Namespace> {

    @Override
    protected org.joyqueue.domain.Namespace forward(Namespace namespace) {
        org.joyqueue.domain.Namespace nsrNameSpace = new org.joyqueue.domain.Namespace();
        if (namespace.getCode() != null) {
            nsrNameSpace.setCode(namespace.getCode());
        }
        if (namespace.getName() != null) {
            nsrNameSpace.setName(namespace.getName());
        }
        return nsrNameSpace;
    }

    @Override
    protected Namespace backward(org.joyqueue.domain.Namespace nsrNamespace) {
        Namespace namespace = new Namespace();
        namespace.setId(nsrNamespace.getCode());
        namespace.setCode(nsrNamespace.getCode());
        namespace.setName(nsrNamespace.getName());
        return namespace;
    }
}
