package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.function.Consumer;

public class F12ViewModel extends ViewModel {
    private MutableLiveData<String> studentInfo = new MutableLiveData<>();
    private MutableLiveData<String> infoResponse = new MutableLiveData<>();
    private MutableLiveData<String> f12Response = new MutableLiveData<>();
    private MutableLiveData<Integer> totalPnts = new MutableLiveData<>();
    private MutableLiveData<Integer> hiddenPnts = new MutableLiveData<>();
    private MutableLiveData<Float> hiddenAvg = new MutableLiveData<>();
    private MutableLiveData<Float> totalAvg = new MutableLiveData<>();
    private MutableLiveData<DisclosedInfo> disclosedInfo = new MutableLiveData<>();
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideCourse = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideStudent = new MutableLiveData<>();

    public void fetchF12(boolean noPnp, Runnable onSuccess, Consumer<ErrorInfo> onError, Runnable anyway){
        new Thread(() -> {
            F12Fetcher.Result result = F12Fetcher.INSTANCE.fetch(noPnp);

            studentInfo.postValue(result.studentInfo);
            infoResponse.postValue(result.infoResponse);
            f12Response.postValue(result.f12Response);
            totalPnts.postValue(result.totalPnts);
            hiddenPnts.postValue(result.hiddenPnts);
            hiddenAvg.postValue(result.hiddenAvg);
            totalAvg.postValue(result.totalAvg);
            disclosedInfo.postValue(result.disclosedInfo);

            if (result.errorInfo != null)
                onError.accept(result.errorInfo);
            else
                onSuccess.run();

            anyway.run();
        }).start();
    }

    public void recalculateHiddenAvg(boolean noPnp){
        float hiddenAvgF = F12Fetcher.INSTANCE.recalculateHiddenAvg(f12Response.getValue(), noPnp);
        hiddenAvg.postValue(hiddenAvgF);
    }

    public MutableLiveData<String> getInfoResponse() {
        return infoResponse;
    }

    public MutableLiveData<String> getF12Response() {
        return f12Response;
    }

    public MutableLiveData<Integer> getTotalPnts() {
        return totalPnts;
    }

    public MutableLiveData<Integer> getHiddenPnts() {
        return hiddenPnts;
    }

    public MutableLiveData<Float> getHiddenAvg() {
        return hiddenAvg;
    }

    public MutableLiveData<Float> getTotalAvg() {
        return totalAvg;
    }

    public MutableLiveData<DisclosedInfo> getDisclosedInfo() {
        return disclosedInfo;
    }

    public MutableLiveData<String> getStudentInfo() {
        return studentInfo;
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
}
