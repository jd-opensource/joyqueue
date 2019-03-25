package com.jd.journalq.toolkit.util;

import com.google.common.collect.Maps;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * ASMUtils
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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