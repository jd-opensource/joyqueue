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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.Namespace;
import org.joyqueue.nsr.sql.domain.NamespaceDTO;

import java.util.Collections;
import java.util.List;

/**
 * NamespaceConverter
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class NamespaceConverter {

    public static NamespaceDTO convert(Namespace namespace) {
        if (namespace == null) {
            return null;
        }
        NamespaceDTO namespaceDTO = new NamespaceDTO();
        namespaceDTO.setId(namespace.getCode());
        namespaceDTO.setCode(namespace.getCode());
        namespaceDTO.setName(namespace.getName());
        return namespaceDTO;
    }

    public static Namespace convert(NamespaceDTO namespaceDTO) {
        if (namespaceDTO == null) {
            return null;
        }
        Namespace namespace = new Namespace();
        namespace.setCode(namespaceDTO.getCode());
        namespace.setName(namespaceDTO.getName());
        return namespace;
    }

    public static List<Namespace> convert(List<NamespaceDTO> namespaceDTOList) {
        if (CollectionUtils.isEmpty(namespaceDTOList)) {
            return Collections.emptyList();
        }
        List<Namespace> result = Lists.newArrayListWithCapacity(namespaceDTOList.size());
        for (NamespaceDTO namespaceDTO : namespaceDTOList) {
            result.add(convert(namespaceDTO));
        }
        return result;
    }
}