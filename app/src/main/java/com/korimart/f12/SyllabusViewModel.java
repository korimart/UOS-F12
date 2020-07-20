package com.korimart.f12;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SyllabusViewModel extends ViewModel {
    private MutableLiveData<String> systemMessage = new MutableLiveData<>();

    private ErrorReporter errorReporter = ErrorReporter.INSTANCE;

    public void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity,
                      String schoolYear, String semester,
                      String curriNumber, String classNumber,
                      boolean uab, String certDivCode){
        systemMessage.setValue("가져오는 중...");
        wiseViewModel.getSyllabus().setValue(null);

        wiseViewModel
                .fetchAndParseSyllabus(
                        true, schoolYear, semester, curriNumber, classNumber, uab, certDivCode)
                .exceptionally(t -> {
                    errorReporter.backgroundErrorHandler(t, errorInfo ->
                            this.onError(errorInfo, mainActivity));
                    return null;
                });
    }

    private void onError(ErrorInfo errorInfo, MainActivity mainActivity) {
        switch (errorInfo.type){
            case sessionExpired:
                mainActivity.goToLoginFrag(1);
                break;

            case timeout:
            case responseFailed:
                systemMessage.setValue("연결 실패 - 페이지를 찾을 수 없습니다");
                break;

            case parseFailed:
                systemMessage.setValue("정보추출 실패 - 개발자에게 문의하세요");
                break;

            default:
                mainActivity.goToErrorFrag(errorInfo.throwable);
                break;
        }
    }

    public LiveData<String> getSystemMessage() {
        return systemMessage;
    }
}
