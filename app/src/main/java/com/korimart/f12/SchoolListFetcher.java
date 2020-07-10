package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;

public enum SchoolListFetcher {
    INSTANCE;

    private WebService webService = WebService.INSTANCE;

    public static class Result {
        HashMap<String, List<String>> schoolToDepts;
        ErrorInfo errorInfo;
    }

    public void parse(Document doc, Result result){
        NodeList univList = doc.getElementsByTagName("univList");
        Node node = univList.item(0);
        return;
    }
}
