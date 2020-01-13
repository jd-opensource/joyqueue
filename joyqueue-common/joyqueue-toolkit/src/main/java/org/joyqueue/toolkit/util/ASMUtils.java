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
package org.joyqueue.toolkit.util;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ASMUtils
 *
 * author: gaohaoxiang
 * date: 2018/10/16
 */
public class ASMUtils {

    private static final ConcurrentMap<String /** class **/, ClassReader> classReaderCache = Maps.newConcurrentMap();
    private static final ConcurrentMap<Class<?> /** class **/, ConcurrentMap<String /** methodName **/, Map<String, Class<?>>>> paramsCache = Maps.newConcurrentMap();

    /**
     * 获得方法参数名称和类型, 基本类型会转换为包装类型
     * 没处理重载方法
     *
     * @param type
     * @param methodName
     * @return
     */
    public static Map<String /** name **/, Class<?> /** type**/> getParams(Class<?> type, String methodName) {
        ConcurrentMap<String, Map<String, Class<?>>> methodParamsCache = paramsCache.get(type);
        if (methodParamsCache == null) {
            methodParamsCache = new ConcurrentHashMap<>();
            ConcurrentMap<String, Map<String, Class<?>>> oldMethodParamsCache = paramsCache.putIfAbsent(type, methodParamsCache);
            if (oldMethodParamsCache != null) {
                methodParamsCache = oldMethodParamsCache;
            }
        }

        Map<String, Class<?>> result = methodParamsCache.get(methodName);
        if (result != null) {
            return result;
        }

        result = doGetParams(type, methodName);
        methodParamsCache.put(methodName, result);
        return result;
    }

    protected static Map<String /** name **/, Class<?> /** type**/> doGetParams(Class<?> type, String methodName) {
        if (type.isInterface()) {
            throw new UnsupportedOperationException();
        }

        Method[] targetMethod = new Method[1];
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName)) {
                targetMethod[0] = method;
                break;
            }
        }

        if (targetMethod[0] == null) {
            return Collections.emptyMap();
        }

        Map<String, Class<?>> result = Maps.newLinkedHashMap();

        getClassReader(type).accept(new ClassVisitor(Opcodes.ASM6) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!name.equals(methodName)) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                return new MethodVisitor(Opcodes.ASM6) {
                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        if (!name.equals("this") && (index - 1) <= targetMethod[0].getParameterTypes().length) {
                            result.put(name, convertType(descriptor));
                        }
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

    protected static ClassReader getClassReader(Class<?> type) {
        String className = type.getName();
        ClassReader classReader = classReaderCache.get(className);
        if (classReader == null) {
            try {
                classReader = new ClassReader(className.replace(".", "/"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ClassReader oldClassReader = classReaderCache.putIfAbsent(className, classReader);
            if (oldClassReader != null) {
                classReader = oldClassReader;
            }
        }
        return classReader;
    }
}