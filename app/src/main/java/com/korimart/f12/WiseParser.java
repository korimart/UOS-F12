package com.korimart.f12;

import org.w3c.dom.Document;

public interface WiseParser {
    interface Result {
        ErrorInfo getErrorInfo();
    }

    void parse(Document doc, WiseParser.Result result);
}
