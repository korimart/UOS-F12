package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class F12ViewModel extends ViewModel {
    public static final String f12URL = "https://wise.uos.ac.kr/uosdoc/ugd.UgdOtcmInq.do";
    public static final String f12InfoParams = "_dept_authDept=auth&_code_smtList=CMN31&&_COMMAND_=onload&&_XML_=XML&_strMenuId=stud00320&";
    public static final String f12Params = "strSchYear=%d&strSmtCd=%s&strStudId=123123&strDiv=2&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00320&";

    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideCourse = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideStudent = new MutableLiveData<>();

    private MutableLiveData<WiseFetcher.Result> f12InfoFetched = new MutableLiveData<>();
    private MutableLiveData<F12InfoParser.Result> f12InfoParsed = new MutableLiveData<>();
    private MutableLiveData<WiseFetcher.Result> f12Fetched = new MutableLiveData<>();
    private MutableLiveData<F12Parser.Result> f12Parsed = new MutableLiveData<>();

    private CompletableFuture<Void> f12Future;

    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;
    private F12Parser f12Parser = new F12Parser();
    private F12InfoParser f12InfoParser = F12InfoParser.INSTANCE;

    public CompletableFuture<Void> fetchAndParse(boolean noPnp, boolean refetch){
        if (f12Future == null || refetch){
            f12Future = CompletableFuture.runAsync(() -> {
                F12Parser.Result f12Result = new F12Parser.Result();
                F12InfoParser.Result f12InfoResult = new F12InfoParser.Result();

                if (!WiseHelper.INSTANCE.fetchAndParse(
                        f12URL, f12InfoParams, wiseFetcher, this.f12InfoFetched,
                        f12InfoParser, this.f12InfoParsed, f12InfoResult))
                    return;

                f12Parser.setNoPnp(noPnp);
                String f12ParamsFormatted = String.format(Locale.US,
                        f12Params, f12InfoResult.schoolYear, f12InfoResult.semester);

                if (!WiseHelper.INSTANCE.fetchAndParse(
                        f12URL, f12ParamsFormatted, wiseFetcher, this.f12Fetched,
                        f12Parser, this.f12Parsed, f12Result))
                    return;
            });
        }

        return f12Future;
    }

    public void recalculateHiddenAvg(boolean noPnp){
        F12Parser.Result f12Parsed = this.f12Parsed.getValue();
        WiseFetcher.Result f12Fetched = this.f12Fetched.getValue();

        if (f12Parsed == null || f12Fetched == null)
            return;

        f12Parser.setNoPnp(noPnp);
        f12Parsed.hiddenAvg = f12Parser.recalculateHiddenAvg(f12Fetched.response);
        this.f12Parsed.postValue(f12Parsed);
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

    public MutableLiveData<F12Parser.Result> getF12Parsed() {
        return f12Parsed;
    }

    public MutableLiveData<F12InfoParser.Result> getF12InfoParsed() {
        return f12InfoParsed;
    }

    public MutableLiveData<WiseFetcher.Result> getF12InfoFetched() {
        return f12InfoFetched;
    }

    public MutableLiveData<WiseFetcher.Result> getF12Fetched() {
        return f12Fetched;
    }
}
