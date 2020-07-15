package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class LoginFragment extends Fragment {
    private EditText idEdit;
    private EditText passwordEdit;
    private Button okButton;
    private TextView systemMessage;
    private int nextFrag = 0;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        loginViewModel = vmp.get(LoginViewModel.class);

        idEdit = view.findViewById(R.id.login_id);
        passwordEdit = view.findViewById(R.id.login_password);
        okButton = view.findViewById(R.id.login_okButton);
        systemMessage = view.findViewById(R.id.login_systemMessage);

        ((MainActivity) getActivity()).getBottomNav().setVisibility(View.INVISIBLE);

        if (savedInstanceState != null)
            nextFrag = savedInstanceState.getInt("onLogin");

        setViewListeners();

        loginViewModel.fetchLoginInfo(getActivity().getApplicationContext())
                .thenRun(() -> {
                    loginViewModel.getLoginInfoReady().postValue(true);
                });
    }

    private void setViewListeners() {
        idEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                idEdit.setHint("");
            else
                idEdit.setHint("아이디");
        });

        passwordEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                passwordEdit.setHint("");
            else
                passwordEdit.setHint("비밀번호");
        });

        okButton.setOnClickListener((v) -> {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(idEdit.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(passwordEdit.getWindowToken(), 0);
            okButton.setEnabled(false);

            loginViewModel.setShouldWriteToFile(true);
            tryLogin(idEdit.getText().toString(), passwordEdit.getText().toString());
        });

        loginViewModel.getMessage().observe(this, message -> systemMessage.setText(message));
        loginViewModel.getMessageColor().observe(this, color -> systemMessage.setTextColor(color));

        loginViewModel.getLoginTryComplete().observe(this, b -> {
            if (b == null || !b) return;

            anyway();
            loginViewModel.getLoginTryComplete().setValue(false);

            if (loginViewModel.getErrorInfo() != null){
                onError(loginViewModel.getErrorInfo());
                return;
            }

            onSuccess();
        });

        loginViewModel.getLoginInfoReady().observe(this, b -> {
            if (b == null || !b) return;

            loginViewModel.getLoginInfoReady().setValue(false);

            if (loginViewModel.getErrorInfo() != null){
                onError(loginViewModel.getErrorInfo());
                return;
            }

            loginViewModel.setShouldWriteToFile(false);
            tryLogin(loginViewModel.getId(), loginViewModel.getPassword());
        });
    }

    private void tryLogin(String id, String password){
        loginViewModel.tryLogin(id, password)
                .thenRun(() -> loginViewModel.getLoginTryComplete().postValue(true));
    }

    private void onError(@NonNull ErrorInfo errorInfo) {
        loginViewModel.getMessageColor().setValue(0xFFFF0000);
        switch (errorInfo.errorType){
            case "timeout":
            case "responseFailed":
                loginViewModel.getMessage().setValue("포털 연결 실패");
                break;

            case "loginFailed":
                loginViewModel.getMessage().setValue("로그인 실패");
                break;

            case "noLoginInfo":
                break;

            default:
                ((MainActivity) getActivity()).goToErrorFrag(errorInfo.callStack);
                break;
        }
    }

    private void onSuccess(){
        if (loginViewModel.isShouldWriteToFile()){
            loginViewModel.writeLoginInfo(
                    getActivity().getApplicationContext(),
                    loginViewModel.getId(),
                    loginViewModel.getPassword());
        }

        goToNextFrag();
    }

    private void anyway(){
        okButton.setEnabled(true);
    }

    private void goToNextFrag() {
        MainActivity mainActivity = ((MainActivity) getActivity());
        mainActivity.getBottomNav().setVisibility(View.VISIBLE);
        switch (nextFrag){
            case 0:
            mainActivity.goToF12Frag();
            break;

            case 1:
            mainActivity.goToCoursesFrag();
            break;
        }
    }
}
