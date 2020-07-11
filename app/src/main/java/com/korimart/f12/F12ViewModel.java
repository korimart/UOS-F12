package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.function.Consumer;

public class F12ViewModel extends ViewModel {
    private MutableLiveData<F12Fetcher.Result> result = new MutableLiveData<>();
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideCourse = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideStudent = new MutableLiveData<>();

    public void fetchF12(boolean noPnp, Runnable onSuccess, Consumer<ErrorInfo> onError, Runnable anyway){
        new Thread(() -> {
            F12Fetcher.Result result = F12Fetcher.INSTANCE.fetch(noPnp);
            this.result.postValue(result);

            if (result.errorInfo != null)
                onError.accept(result.errorInfo);
            else
                onSuccess.run();

            anyway.run();
        }).start();
    }

    public void recalculateHiddenAvg(boolean noPnp){
        F12Fetcher.Result result = this.result.getValue();
        result.hiddenAvg = F12Fetcher.INSTANCE.recalculateHiddenAvg(result.f12Response, noPnp);
        this.result.postValue(result);
    }

    public MutableLiveData<F12Fetcher.Result> getResult() {
        return result;
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
