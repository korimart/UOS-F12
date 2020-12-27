package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public enum XMLHelper {
    INSTANCE;

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;

    public Document getDocument(String xml, String encoding){
        ByteArrayInputStream input = null;

        try {
            input = new ByteArrayInputStream(xml.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Document doc = null;

        if (builder == null){
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException ignore) {
            }
        }

        try {
            doc = builder.parse(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    public Document getDocument(byte[] xml){
        Document doc = null;

        if (builder == null){
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException ignore) {
            }
        }

        try {
            doc = builder.parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    public String getContentByName(Document doc, String name){
        NodeList nl = doc.getElementsByTagName(name);
        Node n = nl.item(0);

        if (n == null) return "";

        Node nc = n.getFirstChild();
        return nc == null ? "" : nc.getTextContent();
    }

    public String getContentByName(Element e, String name){
        NodeList nl = e.getElementsByTagName(name);
        Node n = nl.item(0);

        if (n == null) return "";

        Node nc = n.getFirstChild();
        return nc == null ? "" : nc.getTextContent();
    }

    public String getLastContentByName(Document doc, String name){
        NodeList nl = doc.getElementsByTagName(name);
        Node n = nl.item(nl.getLength() - 1);

        if (n == null) return "";

        Node nc = n.getFirstChild();
        return nc.getTextContent();
    }

    public Element getElementByName(Element root, String name){
        return (Element) root.getElementsByTagName(name).item(0);
    }

    public Element getElementByName(Document root, String name){
        return (Element) root.getElementsByTagName(name).item(0);
    }
}
