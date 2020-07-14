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
    private MutableLiveData<Boolean> f12Ready = new MutableLiveData<>();

    private WiseFetcher.Result f12InfoFetched;
    private F12InfoParser.Result f12InfoParsed;
    private WiseFetcher.Result f12Fetched;
    private F12Parser.Result f12Parsed;

    private CompletableFuture<Void> f12Future;

    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;
    private F12Parser f12Parser = new F12Parser();
    private F12InfoParser f12InfoParser = F12InfoParser.INSTANCE;

    public CompletableFuture<Void> fetchAndParse(boolean noPnp, boolean refetch){
        if (f12Future == null || refetch){
            f12Future = CompletableFuture.runAsync(() -> {
                f12InfoFetched = wiseFetcher.fetch(f12URL, f12InfoParams);
                if (f12InfoFetched.errorInfo != null) return;
                f12InfoParsed = f12InfoParser.parse(f12InfoFetched.document);
                if (f12InfoParsed.errorInfo != null) return;

                f12Parser.setNoPnp(noPnp);
                String f12ParamsFormatted = String.format(Locale.US,
                        f12Params, f12InfoParsed.schoolYear, f12InfoParsed.semester);

                f12Fetched = wiseFetcher.fetch(f12URL, f12ParamsFormatted);
                if (f12Fetched.errorInfo != null) return;
                f12Parsed = f12Parser.parse(f12Fetched.document);
            });
        }

        return f12Future;
    }

    public void recalculateHiddenAvg(boolean noPnp){
        if (f12Parsed == null || f12Fetched == null)
            return;

        f12Parser.setNoPnp(noPnp);
        f12Parsed.hiddenAvg = f12Parser.recalculateHiddenAvg(f12Fetched.response);
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

    public WiseFetcher.Result getF12InfoFetched() {
        return f12InfoFetched;
    }

    public F12InfoParser.Result getF12InfoParsed() {
        return f12InfoParsed;
    }

    public WiseFetcher.Result getF12Fetched() {
        return f12Fetched;
    }

    public F12Parser.Result getF12Parsed() {
        return f12Parsed;
    }

    public MutableLiveData<Boolean> getF12Ready() {
        return f12Ready;
    }
}
