package com.jd.journalq.registry.memory;

import com.jd.journalq.toolkit.io.Compressors;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.registry.RegistryException;
import com.jd.journalq.toolkit.io.Zlib;
import com.jd.journalq.toolkit.lang.Charsets;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * XML配置节点数据
 * Created by hexiaofeng on 15-7-10.
 */
public class XmlRegistry extends MemoryRegistry {


    public static final String NODE = "node";
    public static final String PATH = "path";
    public static final String COMPRESS = "compress";
    public static final String ROOT = "root";

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (url == null || url.getPath() == null) {
            throw new IllegalStateException("url can not be null.");
        }
    }

    @Override
    protected void doStart() throws RegistryException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(url.getPath());
        if (in == null) {
            throw new RegistryException("url is invalid.");
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(in);

            Node node;
            NamedNodeMap attributes;
            Node path;
            Node compress;
            String text;
            byte[] data;
            NodeList nodes = document.getChildNodes();
            // 遍历节点
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                if (node != null && node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(ROOT)) {
                    // 找到根节点，获取子节点
                    nodes = node.getChildNodes();
                    // 遍历子节点
                    for (int j = 0; j < nodes.getLength(); j++) {
                        node = nodes.item(j);
                        // 匹配的节点
                        if (node != null && node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName()
                                .equals(NODE)) {
                            attributes = node.getAttributes();
                            path = attributes.getNamedItem(PATH);
                            compress = attributes.getNamedItem(COMPRESS);
                            text = node.getTextContent();
                            if (text == null) {
                                text = "";
                            }
                            // 确保设置路路径
                            if (path != null) {
                                if (compress != null && Boolean.valueOf(compress.getNodeValue())) {
                                    create(path.getNodeValue(),
                                            Compressors.compress(text, Charsets.UTF_8, Zlib.INSTANCE));
                                } else {
                                    create(path.getNodeValue(), text.getBytes(Charsets.UTF_8));
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } catch (ParserConfigurationException e) {
            throw new RegistryException(e);
        } catch (SAXException e) {
            throw new RegistryException(e);
        } catch (IOException e) {
            throw new RegistryException(e);
        } finally {
            Close.close(in);
        }
    }

    @Override
    public String type() {
        return "xml";
    }
}
