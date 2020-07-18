package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class F12ViewModel extends ViewModel {
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Integer> messageColor = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideCourse = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideStudent = new MutableLiveData<>();
    private MutableLiveData<Boolean> refreshButton = new MutableLiveData<>();

    public MutableLiveData<String> getMessage() {
        return message;
    }

    public MutableLiveData<Integer> getMessageColor() {
        return messageColor;
    }

    public MutableLiveData<Boolean> getHideCourse() {
        return hideCourse;
    }

    public MutableLiveData<Boolean> getHideStudent() {
        return hideStudent;
    }

    public MutableLiveData<Boolean> getRefreshButton() {
        return refreshButton;
    }
}
