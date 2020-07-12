package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LoginFragment extends Fragment {
    private static String loginParams = "_COMMAND_=LOGIN&strTarget=MAIN&strIpAddr=123.123.123.123" +
            "&strMacAddr=123.123.123.123&login_div_1_nm=%%C7%%D0%%BB%%FD&strLoginId=%s&strLoginPw=%s";
    private static String loginURL = "https://wise.uos.ac.kr/uosdoc/com.StuLogin.serv";
    private String id;
    private String password;
    private EditText idEdit;
    private EditText passwordEdit;
    private Button okButton;
    private TextView systemMessage;
    private int nextFrag = 0;
    private F12ViewModel f12ViewModel;
    private MainViewModel mainViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        f12ViewModel = vmp.get(F12ViewModel.class);
        mainViewModel = vmp.get(MainViewModel.class);

        idEdit = view.findViewById(R.id.login_id);
        passwordEdit = view.findViewById(R.id.login_password);
        okButton = view.findViewById(R.id.login_okButton);
        systemMessage = view.findViewById(R.id.login_systemMessage);

        ((MainActivity) getActivity()).getBottomNav().setVisibility(View.INVISIBLE);

        setViewListeners();

        File internalPath = getActivity().getFilesDir();
        Path loginInfoPath = Paths.get(internalPath.getPath(), "loginInfo.txt");
        if (loginInfoPath.toFile().isFile()){
            try {
                List<String> loginInfo = Files.readAllLines(loginInfoPath);
                if (loginInfo.size() >= 2){
                    id = loginInfo.get(0);
                    password = loginInfo.get(1);
                }
            } catch (IOException ignore) {
            }
        }

        if (id != null){
            (new Thread(() -> tryLogin(true))).start();
        }

        if (savedInstanceState != null)
            nextFrag = savedInstanceState.getInt("onLogin");
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
            id = idEdit.getText().toString();
            password = passwordEdit.getText().toString();
            (new Thread(() -> tryLogin(false))).start();
        });
    }

    private void tryLogin(boolean fromFile) {
        okButton.post(() -> okButton.setEnabled(false));
        try {
            String response = WebService.INSTANCE.sendPost(loginURL, String.format(loginParams, id, password), "euc-kr");
            if (response.contains("전산실 요청에 의해 제거함"))
                throw new Exception();
        } catch (Exception e) {
            systemMessage.post(() -> systemMessage.setText("로그인 실패"));
            okButton.post(() -> okButton.setEnabled(true));
            return;
        }

        if (!fromFile){
            try {
                OutputStreamWriter osw = new OutputStreamWriter(
                        getActivity().openFileOutput("loginInfo.txt", Context.MODE_PRIVATE));

                osw.write(id + "\n" + password);
                osw.flush();
                osw.close();
            } catch (IOException ignore) {
            }
        }

        MainActivity mainActivity = ((MainActivity) getActivity());
        mainActivity.runOnUiThread(() -> {
            mainActivity.getBottomNav().setVisibility(View.VISIBLE);
            f12ViewModel.fetchF12(mainViewModel.getNoPnp().getValue(), () -> {}, (errorInfo) -> {}, () -> {});

            switch (nextFrag){
                case 0:
                mainActivity.goToF12Frag();
                break;

                case 1:
                mainActivity.goToCoursesFrag();
                break;
            }
        });
    }
}
