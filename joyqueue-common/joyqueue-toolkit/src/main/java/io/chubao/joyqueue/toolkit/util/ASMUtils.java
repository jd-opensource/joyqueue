/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.toolkit.util;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * ASMUtils
 *
 * author: gaohaoxiang
 * date: 2018/10/16
 */
public class ASMUtils {

    private static final ConcurrentMap<String /** class **/, ClassReader> classReaderCache = Maps.newConcurrentMap();

    /**
     * 获得方法参数名称和类型, 最好不要频繁调用
     * 会按照参数顺序返回，基本类型会转换为包装类型
     * 没处理重载方法
     *
     * @param type
     * @param method
     * @return
     */
    // TODO 简单实现，需要过滤掉局部变量
    public static Map<String /** name **/, Class<?> /** type**/> getParams(Class<?> type, String method) {
        if (type.isInterface()) {
            throw new UnsupportedOperationException();
        }

        String className = type.getName();
        ClassReader classReader = classReaderCache.get(className);
        if (classReader == null) {
            try {
                classReader = new ClassReader(className.replace(".", "/"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (classReaderCache.putIfAbsent(className, classReader) != null) {
                classReader = classReaderCache.get(className);
            }
        }

        Map<String, Class<?>> result = Maps.newLinkedHashMap();
        classReader.accept(new ClassVisitor(Opcodes.ASM6) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!name.equals(method)) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                return new MethodVisitor(Opcodes.ASM6) {
                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        if (name.equals("this")) {
                            return;
                        }
                        result.put(name, convertType(descriptor));
                        super.visitLocalVariable(name, descriptor, signature, start, end, index);
                    }
                };
            }
        }, 0);

        return result;
    }

    public static Class<?> convertType(String type) {
        try {
            if (type.startsWith("L") && type.endsWith(";")) {
                return Class.forName(type.substring(1, type.length() - 1).replace("/", "."));
            } else {
                switch (type) {
                    case "I":
                        return Integer.class;
                    case "S":
                        return Short.class;
                    case "B":
                        return Byte.class;
                    case "F":
                        return Float.class;
                    case "D":
                        return Double.class;
                    case "J":
                        return Long.class;
                    case "Z":
                        return Boolean.class;
                    default:
                        throw new UnsupportedOperationException(type);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}