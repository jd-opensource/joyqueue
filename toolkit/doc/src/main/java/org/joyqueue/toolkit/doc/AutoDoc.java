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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Auto doc for routing file and it's related service interface
 **/
public class AutoDoc {
    private Logger logger = LoggerFactory.getLogger(AutoDoc.class);
    /**
     * routing file path
     **/
    private List<String> routes;

    /**
     * service
     **/
    private List<String> pkgNames;
    private Map<String, Map<String, APIDoc>> serviceMethodRouteDocs;
    private File out;
    private RandomAccessFile rFile;
    private FileChannel channel;
    private ByteBuffer buffer;
    private Map<String/*service name*/, Map<String /*method name*/, JavadocComment>> serviceMethodDocs;

    public AutoDoc(File out, List<String> routes, List<String> pkgNames) {
        this.out = out;
        this.routes = routes;
        this.pkgNames = pkgNames;
        initDocFile();
    }


    /**
     * init doc file and buffer
     **/
    private void initDocFile() {
        try {
            if (!out.exists()) {
                //file.
                //out.delete();
                if (out.createNewFile()) {
                    logger.info("create file:" + out.getName());
                }
            }
            rFile = new RandomAccessFile(out, "rw");
            rFile.setLength(0); //clear
            channel = rFile.getChannel();
            buffer = ByteBuffer.allocateDirect(8 * 4096);
        } catch (IOException e) {
            logger.info("init doc file error", e);
        }
    }

    /**
     * Scan servers and routing file and auto generate doc for them
     *
     * @param apiDocFormat api doc formatter
     **/
    public void write(Format<APIDoc> apiDocFormat, Class<? extends MultiHandlerMetaParser<APIDoc>> routingParserClass,
                      HeuristicAutoTest<APIDoc> tester) throws Exception {
        serviceMethodRouteDocs = new HashMap<>(8);
        Map<String, PackageDocScanParser> servicePackageMap = new HashMap(8);
        Constructor<? extends MultiHandlerMetaParser<APIDoc>> routingParserConstructor = routingParserClass.getConstructor(String.class);
        for (String r : routes) {
            MultiHandlerMetaParser<APIDoc> routingParser = routingParserConstructor.newInstance(r);
            serviceMethodRouteDocs.putAll(routingParser.parse());
        }
        serviceMethodDocs = new HashMap(8);
        for (String pkgName : pkgNames) {
            PackageDocScanParser pkgScanner = new PackageDocScanParser(pkgName);
            Map<String, Map<String, JavadocComment>> pkgServiceMethodDocs = pkgScanner.parse();
            serviceMethodDocs.putAll(pkgServiceMethodDocs);
            for (String service : pkgServiceMethodDocs.keySet()) {
                servicePackageMap.put(service, pkgScanner);
            }
        }
        StringBuilder methodDesBuilder = new StringBuilder();
        for (Map.Entry<String, Map<String, APIDoc>> serviceEntry : serviceMethodRouteDocs.entrySet()) {
            String serviceName = serviceEntry.getKey();
            String upperServiceName = Character.toUpperCase(serviceName.charAt(0)) + serviceName.substring(1);
            for (Map.Entry<String, APIDoc> apiDocEntry : serviceEntry.getValue().entrySet()) {
                String methodName = apiDocEntry.getKey();
                APIDoc doc = apiDocEntry.getValue();
                if (doc != null) {
                    doc.setService(upperServiceName);
                    doc.setMethod(methodName);
                    Map<String, JavadocComment> javadocCommentMap = serviceMethodDocs.get(upperServiceName);
                    if (javadocCommentMap != null) {
                        JavadocComment comment = javadocCommentMap.get(methodName);
                        if (comment != null) {
                            fillParamDoc(doc, comment, methodDesBuilder);
                        }
                    }
                }
            }
        }
        // test
        for (Map<String, APIDoc> methodDoc : serviceMethodRouteDocs.values()) {
            for (APIDoc doc : methodDoc.values()) {
                try {
                    PackageDocScanParser serviceDocParser = servicePackageMap.get(doc.getService());
                    if (serviceDocParser != null) {
                        List<Class> paramClasses = serviceDocParser.getNonRawParams(doc.getService(), doc.getMethod());
                        if (paramClasses == null) {
                            TestCase result = tester.test(paramClasses, doc);
                            doc.setiDemo(result.getRequest());
                            doc.setoDemo(result.getResponse());
                        }
                    }
                } catch (Exception e) {
                    logger.info("test error", e);
                }
            }
        }
        writeFile(serviceMethodRouteDocs, apiDocFormat);
    }


    /**
     * doc map write to destination file
     **/
    private void writeFile(Map<String, Map<String, APIDoc>> apiDocs, Format<APIDoc> format) {
        try {
            Long sequenceId = 0L;
            for (Map.Entry<String, Map<String, APIDoc>> e : apiDocs.entrySet()) {
                for (Map.Entry<String, APIDoc> apiDocEntry : e.getValue().entrySet()) {
                    APIDoc doc = apiDocEntry.getValue();
                    sequenceId++;
                    bufferWrite(format.format(sequenceId.toString(), doc));
                }
            }

        } catch (IOException e) {
            logger.info("io exception", e);
        }


    }

    /**
     * get all param type
     **/
    public Set<Param> getParamKeys() {
        Set<Param> params = new HashSet<>();
        for (Map.Entry<String, Map<String, APIDoc>> e : serviceMethodRouteDocs.entrySet()) {
            for (Map.Entry<String, APIDoc> apiDocEntry : e.getValue().entrySet()) {
                APIDoc doc = apiDocEntry.getValue();
                if (doc.getParams() != null) {
                    params.addAll(doc.getParams());
                }
            }
        }
        return params;
    }


    private synchronized void bufferWrite(String docContent) throws IOException {
        byte[] bytes = docContent.getBytes();
        if (buffer.remaining() > bytes.length) {
            buffer.put(bytes);
        } else {
            int byteLen = bytes.length;
            int offset = 0;
            do {
                int len = buffer.remaining();
                buffer.put(bytes, offset, Math.min(len, byteLen));
                buffer.flip();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                buffer.flip();
                offset += len;
                byteLen -= len;
            } while (byteLen > 0);
        }
    }


    public synchronized void close() throws IOException {
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.close();
        rFile.close();
    }

    /**
     * Write the method parameters and it's doc to target api doc
     *
     * @param doc     target doc
     * @param comment parameters and method doc
     **/
    private void fillParamDoc(APIDoc doc, JavadocComment comment, StringBuilder methodDesBuilder) {
        Javadoc javadoc = comment.parse();
        toString(javadoc.getDescription(), methodDesBuilder);
        doc.setDesc(methodDesBuilder.toString());
        methodDesBuilder.setLength(0);
        List<JavadocBlockTag> tags = javadoc.getBlockTags();
        if (comment.getCommentedNode().isPresent()) {
            Node node = comment.getCommentedNode().get();
            if (node instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) node;
                for (Parameter p : method.getParameters()) {
                    String type = p.getType().asString();
                    String name = p.getName().asString();
                    List<Param> params = doc.getParams();
                    Param param = new Param();
                    param.setName(name);
                    param.setType(type);
                    for (JavadocBlockTag t : tags) {
                        if (t.getName().isPresent()) {
                            if (name.endsWith(t.getName().get())) {
                                toString(t.getContent(), methodDesBuilder);
                                param.setComment(methodDesBuilder.toString());
                                methodDesBuilder.setLength(0);
                            }
                        }
                    }
                    if (params == null) {
                        params = new ArrayList<>();
                        doc.setParams(params);
                    }
                    params.add(param);
                }
            }
        }
    }


    /**
     * JavadocDescription toString
     **/
    private void toString(JavadocDescription description, StringBuilder content) {
        for (JavadocDescriptionElement e : description.getElements()) {
            content.append(e.toText()).append(",");
        }
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
        }
    }


}

