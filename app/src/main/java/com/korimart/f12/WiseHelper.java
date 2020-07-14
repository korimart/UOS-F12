package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;

public enum WiseHelper {
    INSTANCE;

    public <T extends WiseParser.Result> boolean fetchAndParse(String url, String param,
                                 WiseFetcher wiseFetcher,
                                 MutableLiveData<WiseFetcher.Result> mldFetched,
                                 WiseParser wiseParser,
                                 MutableLiveData<T> mldParsed,
                                 T parserResultImp){
        WiseFetcher.Result fetched = wiseFetcher.fetch(url, param);
        mldFetched.postValue(fetched);
        if (fetched.errorInfo != null){
            return false;
        }

        wiseParser.parse(fetched.document);
        mldParsed.postValue(parserResultImp);
        if (parserResultImp.getErrorInfo() != null){
            return false;
        }

        return true;
    }
}
