package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.nsr.journalkeeper.domain.NamespaceDTO;
import org.apache.commons.collections.CollectionUtils;

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