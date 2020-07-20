package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AsyncFetchParser {
    public static class ResultCache <T> {
        MutableLiveData<T> resultLiveData = new MutableLiveData<>();
        CompletableFuture<T> future;
        T data;
    }

    private ResultCache<WiseFetcher.Result> fCache = new ResultCache<>();
    private ResultCache<WiseParser.Result> pCache = new ResultCache<>();
    private String url;
    private String params;
    private WiseFetcher wiseFetcher = WiseFetcher.INSTANCE;
    private WiseParser wiseParser;

    public AsyncFetchParser(String url, String params, WiseParser wiseParser){
        this.url = url;
        this.params = params;
        this.wiseParser = wiseParser;
    }

    public CompletableFuture<Void> fetch(boolean refetch){
        if (refetch)
            cancelAndClear(fCache);

        if (fCache.future == null){
            createFetchFuture(url, params, fCache);
            return fCache.future.thenAccept(result -> {
                fCache.data = result;
                fCache.resultLiveData.postValue(result);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> parse(boolean reparse){
        if (reparse)
            cancelAndClear(pCache);

        if (pCache.future == null){
            createParseFuture();
            return pCache.future.thenAccept(result -> {
                pCache.data = result;
                pCache.resultLiveData.postValue(result);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> fetchAndParse(boolean refetch){
        return fetch(refetch).thenCompose(ignored -> parse(refetch));
    }

    public void cancelAndClear(ResultCache<?> resultCache){
        if (resultCache.future != null){
            resultCache.future.cancel(true);
            resultCache.data = null;
            resultCache.future = null;
        }
    }

    private void createFetchFuture(
            String url, String params, ResultCache<WiseFetcher.Result> resultCache){

        resultCache.future = CompletableFuture.supplyAsync(() -> {
            WiseFetcher.Result fetched = wiseFetcher.fetch(url, params);

            if (fetched.errorInfo != null){
                throw new CompletionException(fetched.errorInfo);
            }

            return fetched;
        });
    }

    private void createParseFuture(){
        pCache.future = CompletableFuture.supplyAsync(() -> {
            WiseParser.Result parsed = wiseParser.parse(fCache.data.document);
            if (parsed.getErrorInfo() != null)
                throw new CompletionException(parsed.getErrorInfo());

            return parsed;
        });
    }

    public void setParams(String params){
        this.params = params;
    }

    public ResultCache<WiseFetcher.Result> getfCache() {
        return fCache;
    }

    public ResultCache<WiseParser.Result> getpCache() {
        return pCache;
    }

    public WiseParser getWiseParser() {
        return wiseParser;
    }
}
