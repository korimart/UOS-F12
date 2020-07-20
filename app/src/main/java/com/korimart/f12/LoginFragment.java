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

public class LoginFragment extends Fragment {
    private EditText idEdit;
    private EditText passwordEdit;
    private Button okButton;
    private TextView systemMessage;
    private int nextFrag = 0;
    private LoginViewModel loginViewModel;

    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewModelProvider vmp = new ViewModelProvider(mainActivity, new ViewModelProvider.NewInstanceFactory());
        loginViewModel = vmp.get(LoginViewModel.class);

        idEdit = view.findViewById(R.id.login_id);
        passwordEdit = view.findViewById(R.id.login_password);
        okButton = view.findViewById(R.id.login_okButton);
        systemMessage = view.findViewById(R.id.login_systemMessage);

        mainActivity.getBottomNav().setVisibility(View.INVISIBLE);

        if (savedInstanceState != null)
            nextFrag = savedInstanceState.getInt("onLogin");

        setViewListeners();
        loginViewModel.onViewCreated(mainActivity, this::goToNextFrag);
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
                    (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(idEdit.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(passwordEdit.getWindowToken(), 0);
            loginViewModel.login(
                    mainActivity,
                    idEdit.getText().toString(),
                    passwordEdit.getText().toString(),
                    true,
                    this::goToNextFrag);
        });

        loginViewModel.getMessage().observe(this, message -> {
            systemMessage.setText(message);
            systemMessage.setTextColor(0xFFFF0000);
        });

        loginViewModel.getFetching().observe(this, fetching -> okButton.setEnabled(!fetching));
    }

    private void goToNextFrag() {
        mainActivity.getBottomNav().setVisibility(View.VISIBLE);
        switch (nextFrag){
            case 0:
            mainActivity.getBottomNav().setSelectedItemId(R.id.bottomNav_F12);
            break;

            case 1:
            mainActivity.getBottomNav().setSelectedItemId(R.id.bottomNav_majors);
            break;
        }
    }
}
