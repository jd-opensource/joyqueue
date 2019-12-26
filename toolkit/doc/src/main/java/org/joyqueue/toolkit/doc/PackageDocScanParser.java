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
package org.joyqueue.toolkit.doc;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.joyqueue.toolkit.doc.util.PackageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Scan interface doc under the package
 **/
public class PackageDocScanParser implements MetaParser<JavadocComment> {

    private Logger logger = LoggerFactory.getLogger(PackageDocScanParser.class);
    private String pkg;
    private Map<String/*service*/, Map<String/*method*/, List<Class/*param type*/>>> serviceMethodNonRowParamsType;

    public PackageDocScanParser(String pkg) {
        this.pkg = pkg;
    }

    @Override
    public Map<String, Map<String, JavadocComment>> parse() {
        Map<String, Map<String, JavadocComment>> serviceMethodsMap = new HashMap<>();
        Map<String, Class> classMap = packageScan();
        for (Map.Entry<String, Class> entry : classMap.entrySet()) {
            String path = PackageParser.getSrcPath(entry.getKey());
            File srcFile = new File(path);
            int simpleNameStart = entry.getKey().lastIndexOf(".");
            String serviceName = entry.getKey().substring(simpleNameStart + 1);
            serviceMethodsMap.put(serviceName, parseDoc(srcFile));
        }
        processInterfaceExtend(classMap, serviceMethodsMap);
        return serviceMethodsMap;
    }


    private void processInterfaceExtend(Map<String, Class> classMap, Map<String, Map<String, JavadocComment>> serviceMethodMap) {
        for (Map.Entry<String, Class> entry : classMap.entrySet()) {
            int simpleNameStart = entry.getKey().lastIndexOf(".");
            String serviceName = entry.getKey().substring(simpleNameStart + 1);
            Class[] classes = entry.getValue().getInterfaces();
            for (Class z : classes) {
                Map<String, JavadocComment> methodDocMap = serviceMethodMap.get(serviceName);
                if (methodDocMap != null) {
                    logger.info(z.getName());
                    int nameStart = z.getName().lastIndexOf(".");
                    String name = z.getName().substring(nameStart + 1);
                    Map<String, JavadocComment> childInterfaceDoc = serviceMethodMap.get(name);
                    if (childInterfaceDoc != null && childInterfaceDoc.size() > 0) {
                        methodDocMap.putAll(childInterfaceDoc);
                    }
                }
            }
        }
    }


    /**
     * scan interface class under the package
     **/
    public Map<String, Class> packageScan() {
        Map<String, Class> uniqueClass = new HashMap<>();
        try {
            Class[] classes = PackageParser.scanClass(pkg);
            for (Class s : classes) {
                if (s.isInterface()) {
                    traverse(s, uniqueClass);
                }
            }
            for (String inter : uniqueClass.keySet()) {
                logger.info(inter);
            }
        } catch (Exception e) {

        }
        return uniqueClass;

    }


    /**
     * @return non-raw type parameters of method
     **/
    public List<Class> getNonRawParams(String service, String method) {
        if (serviceMethodNonRowParamsType == null) {
            serviceMethodNonRowParamsType = new HashMap(8);
            Map<String, Class> classMap = packageScan();
            Map<String, List<Method>> serviceMethods = parseServiceMethod(classMap);
            for (Map.Entry<String, List<Method>> e : serviceMethods.entrySet()) {
                List<Method> methods = e.getValue();
                String serviceName = e.getKey();
                for (Method m : methods) {
                    String methodName = m.getName();
                    if (m.getParameters().length > 0) {
                        List<Class> params = new ArrayList<>();
                        for (java.lang.reflect.Parameter p : m.getParameters()) {
                            if (!isPrimitiveType(p.getType())) {
                                params.add(p.getType());
                            }
                        }
                        if (params.size() > 0) {
                            Map<String, List<Class>> methodsMap = serviceMethodNonRowParamsType.get(serviceName);
                            if (methodsMap == null) {
                                methodsMap = new HashMap<>();
                                serviceMethodNonRowParamsType.put(serviceName, methodsMap);
                            }
                            methodsMap.put(methodName, params);
                        }
                    }
                }
            }
        }
        Map<String, List<Class>> methodsMaps = serviceMethodNonRowParamsType.get(service);
        if (methodsMaps != null) {
            return methodsMaps.get(method);
        }
        return null;
    }


    /**
     * java.lang.String
     * org.joyqueue.info
     **/
    public boolean isPrimitiveType(Class clazz) {
        String name = clazz.getCanonicalName();
        try {
            if (name.indexOf(".") > 0 && (!(name.startsWith("java") || name.startsWith("javax")) || clazz.isInterface())) {
                return false;
            }
        } catch (Exception e) {
            logger.info("e", e);
        }
        return true;
    }

    private Map<String, JavadocComment> parseDoc(File classFile) {
        Map<String, JavadocComment> classDoc = new HashMap<>();
        try {
            CompilationUnit cu = StaticJavaParser.parse(classFile, StandardCharsets.UTF_8);
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(JavadocComment comment, Object arg) {
                    super.visit(comment, arg);
                    if (comment.getCommentedNode().get() instanceof MethodDeclaration) {
                        MethodDeclaration node = (MethodDeclaration) comment.getCommentedNode().get();
                        classDoc.put(methodName(node), comment);
                    }
                }
            }.visit(cu, null);
        } catch (Exception e) {
            logger.info("ERROR PROCESSING ", e);
        }
        return classDoc;
    }

    private static String describe(Node node) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) node;
            return "Method " + methodDeclaration.getDeclarationAsString(false, false, true);
        }
        if (node instanceof ConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) node;
            return "Constructor " + constructorDeclaration.getDeclarationAsString();
        }
        if (node instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) node;
            if (classOrInterfaceDeclaration.isInterface()) {
                return "Interface " + classOrInterfaceDeclaration.getName();
            } else {
                return "Class " + classOrInterfaceDeclaration.getName();
            }
        }
        if (node instanceof EnumDeclaration) {
            EnumDeclaration enumDeclaration = (EnumDeclaration) node;
            return "Enum " + enumDeclaration.getName();
        }
        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            List<String> varNames = fieldDeclaration.getVariables().stream().map(v -> v.getName().getId()).collect(Collectors.toList());
            return "Field " + String.join(", ", varNames);
        }
        return node.toString();
    }


    private String methodName(MethodDeclaration node) {
        String methodString = node.getDeclarationAsString(false, false, true);

        String[] returnAndName = methodString.split("\\(");
        if (returnAndName.length >= 2) {
            int start = returnAndName[0].lastIndexOf(" ");
            if (start > 0)
                return returnAndName[0].substring(start + 1);
            else {
                logger.info(methodString);
            }
        }
        return null;
    }

    /**
     * parse service declared method
     **/
    public Map<String, List<Method>> parseServiceMethod(Map<String, Class> classMap) {
        Map<String, List<Method>> serviceMethods = new HashMap<>();
        for (Map.Entry<String, Class> classEntry : classMap.entrySet()) {
            Class clazz = classEntry.getValue();
            List<Method> methods = new ArrayList<>();
            traverseMethod(clazz, methods);
            serviceMethods.put(classEntry.getKey(), methods);
        }
        return serviceMethods;
    }

    /**
     * traverse service declared method
     *
     * @param list
     **/
    private void traverseMethod(Class clazz, List<Method> list) {
        if (clazz.getDeclaredMethods().length > 0) {
            list.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        }
        if (clazz.isInterface() && clazz.getInterfaces().length > 0) {
            for (Class c : clazz.getInterfaces()) {
                traverseMethod(c, list);
            }
        }
    }


    /**
     * Traverse class interface
     **/
    private void traverse(Class clazz, Map<String, Class> interfaces) {
        if (clazz.isInterface()) {
            Class[] classes = clazz.getInterfaces();
            interfaces.put(clazz.getCanonicalName(), clazz);
            if (classes.length > 0) {
                for (Class c : classes) {
                    traverse(c, interfaces);
                }
            }
        }

    }
}
