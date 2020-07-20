package com.korimart.f12;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDateTime;
import java.util.Locale;

public class F12ViewModel extends ViewModel {
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Integer> messageColor = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideCourse = new MutableLiveData<>();
    private MutableLiveData<Boolean> hideStudent = new MutableLiveData<>();
    private MutableLiveData<Boolean> refreshButton = new MutableLiveData<>();

    private boolean firstOpen = true;

    public void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity){
        if (!firstOpen) return;

        firstOpen = false;
        fetch(wiseViewModel, mainActivity, true);
    }

    public void fetch(WiseViewModel wiseViewModel, MainActivity mainActivity, boolean refetch){
        refreshButton.setValue(false);
        message.setValue("가져오는 중...");
        messageColor.setValue(0xFF000000);

        wiseViewModel.fetchAndParseF12(refetch, refetch)
                .whenComplete((ignored, throwable) -> whenDone())
                .thenRun(this::onSuccess)
                .exceptionally(throwable -> {
                    wiseViewModel.errorHandler(throwable, errorInfo -> onError(errorInfo, mainActivity));
                    return null;
                });
    }

    /**
     * should be called from background thread
     */
    private void onSuccess(){
        LocalDateTime dt = LocalDateTime.now();
        message.postValue(
                String.format(
                        Locale.getDefault(),
                        "마지막 새로고침 : %d-%d-%d %d시 %d분 %d초",
                        dt.getYear(), dt.getMonthValue(),
                        dt.getDayOfMonth(), dt.getHour(),
                        dt.getMinute(), dt.getSecond())
        );
        messageColor.postValue(0xFF000000);
    }

    /**
     * should be called from background thread
     */
    private void whenDone(){
        refreshButton.postValue(true);
    }

    private void onError(ErrorInfo errorInfo, MainActivity mainActivity){
        switch (errorInfo.type){
            case sessionExpired:
                mainActivity.goToLoginFrag(0);
                break;

            case timeout:
            case responseFailed:
                message.setValue("성적 불러오기 실패");
                messageColor.setValue(0xFFFF0000);
                refreshButton.setValue(true);
                break;

            case parseFailed:
                message.setValue("와이즈 시스템 방식이 변경된 듯 (F12가 막혔을 수 있음)");
                messageColor.setValue(0xFFFF0000);
                refreshButton.setValue(true);
                break;

            case noOneDisclosedGrade:
                message.setValue("성적이 하나도 안 떠서 볼 수가 없음");
                messageColor.setValue(0xFFFF0000);
                refreshButton.setValue(true);
                break;

            default:
                mainActivity.goToErrorFrag();
                break;
        }
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Integer> getMessageColor() {
        return messageColor;
    }

    public LiveData<Boolean> getRefreshButton() {
        return refreshButton;
    }

    public MutableLiveData<Boolean> getHideCourse() {
        return hideCourse;
    }

    public MutableLiveData<Boolean> getHideStudent() {
        return hideStudent;
    }

}
