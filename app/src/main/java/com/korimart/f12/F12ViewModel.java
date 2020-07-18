package com.korimart.f12;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.w3c.dom.Document;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class F12ViewModel extends ViewModel {
    public static class ResultCache <T> {
        MutableLiveData<T> resultLiveData = new MutableLiveData<>();
        CompletableFuture<T> future;
        T data;
    }

    private ResultCache<WiseFetcher.Result> f12InfoFCache = new ResultCache<>();
    private ResultCache<WiseParser.Result> f12InfoPCache = new ResultCache<>();
    private ResultCache<WiseFetcher.Result> f12FCache = new ResultCache<>();
    private ResultCache<WiseParser.Result> f12PCache = new ResultCache<>();

    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideCourse = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideStudent = new MutableLiveData<>();

    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;
    private F12Parser f12Parser = new F12Parser();
    private F12InfoParser f12InfoParser = F12InfoParser.INSTANCE;
    private Handler handler = new Handler(Looper.getMainLooper());

    public CompletableFuture<Void> fetchF12Info(boolean refetch){
        return simpleFetchWithRefetch(
                URLStorage.getF12URL(),
                URLStorage.getF12InfoParams(),
                f12InfoFCache,
                refetch);
    }

    public CompletableFuture<Void> parseF12Info(Document doc, boolean reparse){
        return simpleParse(doc, f12InfoParser, f12InfoPCache, reparse);
    }

    public CompletableFuture<Void> fetchAndParseF12Info(boolean refetch){
        return fetchF12Info(refetch)
                .thenCompose((ignored) -> {
                    Document doc = f12InfoFCache.data.document;
                    return parseF12Info(doc, refetch);
                });
    }

    public CompletableFuture<Void> fetchF12(boolean refetchInfo, boolean refetch){
        return fetchAndParseF12Info(refetchInfo)
                .thenCompose(ignored -> {
                    F12InfoParser.Result parsed = (F12InfoParser.Result) f12InfoPCache.data;
                    String params = URLStorage.getF12Params(parsed.schoolYear, parsed.semester);
                    return simpleFetchWithRefetch(URLStorage.getF12URL(), params, f12FCache, refetch);
                });
    }

    public CompletableFuture<Void> parseF12(Document doc, boolean reparse){
        return simpleParse(doc, f12Parser, f12PCache, reparse);
    }

    public CompletableFuture<Void> fetchAndParseF12(boolean refetchInfo, boolean refetch){
        return fetchF12(refetchInfo, refetch)
                .thenCompose(ignored -> {
                    Document doc = f12FCache.data.document;
                    return parseF12(doc, refetch);
                });
    }

    private CompletableFuture<Void> simpleFetchWithRefetch(
            String url, String params,
            ResultCache<WiseFetcher.Result> cache, boolean refetch){
        if (refetch)
            cancelAndClear(cache);

        if (cache.future == null){
            createFetchFuture(url, params, cache);
            return cache.future.thenAccept(result -> {
                cache.data = result;
                cache.resultLiveData.postValue(result);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> simpleParse(Document doc, WiseParser parser,
                                                ResultCache<WiseParser.Result> cache,
                                                boolean reparse){
        if (reparse)
            cancelAndClear(cache);

        if (cache.future == null){
            createParseFuture(doc, parser, cache);
            return cache.future.thenAccept(result -> {
                cache.data = result;
                cache.resultLiveData.postValue(result);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    private void cancelAndClear(ResultCache<?> resultCache){
        if (resultCache.future != null){
            resultCache.future.cancel(true);
            resultCache.data = null;
            resultCache.future = null;
            resultCache.resultLiveData.postValue(null);
        }
    }

    private void createParseFuture(Document doc, WiseParser parser,
                                   ResultCache<WiseParser.Result> cache){
        cache.future = CompletableFuture.supplyAsync(() -> {
            WiseParser.Result parsed = parser.parse(doc);
            if (parsed.getErrorInfo() != null)
                throw new CompletionException(parsed.getErrorInfo());

            return parsed;
        });
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

    public void errorHandler(Throwable throwable, Consumer<ErrorInfo> onError){
        if (throwable == null) return;

        Throwable cause = throwable.getCause();

        if (cause instanceof ErrorInfo){
            handler.post(() -> onError.accept((ErrorInfo) cause));
            if (((ErrorInfo) cause).throwable != null)
                ErrorReporter.INSTANCE.reportError(((ErrorInfo) cause).throwable);
        }
        else {
            handler.post(() -> onError.accept(new ErrorInfo(cause)));
            ErrorReporter.INSTANCE.reportError(cause);
        }
    }

    public void recalculateHiddenAvg(boolean noPnp){
        WiseFetcher.Result fetched = f12FCache.resultLiveData.getValue();
        F12Parser.Result parsed = (F12Parser.Result) f12PCache.resultLiveData.getValue();

        if (fetched == null || parsed == null)
            return;

        f12Parser.setNoPnp(noPnp);
        parsed.hiddenAvg = f12Parser.recalculateHiddenAvg(fetched.response);
        f12PCache.resultLiveData.setValue(parsed);
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }

    public MutableLiveData<Boolean> getHideCourse() {
        return hideCourse;
    }

    public MutableLiveData<Boolean> getHideStudent() {
        return hideStudent;
    }

    public LiveData<WiseParser.Result> getF12Parsed(){
        return f12PCache.resultLiveData;
    }
}
