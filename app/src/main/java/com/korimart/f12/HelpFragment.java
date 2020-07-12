package com.korimart.f12;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class HelpFragment extends Fragment {
    private MainViewModel mainViewModel;
    private TextView version;
    private TextView updateMessage;
    private TextView updateLink;
    private TextView announcement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        mainViewModel = vmp.get(MainViewModel.class);

        version = view.findViewById(R.id.help_version);
        updateMessage = view.findViewById(R.id.help_updateMessage);
        updateLink = view.findViewById(R.id.help_updateLink);
        announcement = view.findViewById(R.id.help_announcement);

        setViewListeners();
    }

    private void setViewListeners() {
        mainViewModel.getUpdateLink().observe(this, (s) -> {
            if (s == null) return;
            updateMessage.setText("최신버전이 아닙니다. 본 버전에 오류가 있을 수 있습니다. 다운로드 :");
            updateMessage.setTextColor(0xFFFF0000);
            updateLink.setText(s);
            updateLink.setVisibility(View.VISIBLE);
        });

        mainViewModel.getAnnouncement().observe(this, s -> {
            if (s == null) return;
            announcement.setText(s);
        });

        try {
            PackageInfo pInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            version.setText(String.valueOf(pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }
}
