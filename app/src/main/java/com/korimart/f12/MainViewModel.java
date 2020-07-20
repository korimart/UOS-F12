package com.korimart.f12;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MainViewModel extends ViewModel {
    private static String updateInfoURL = "https://korimart.github.io/UOS-12/updateInfo.txt";
    private static String updateLinkURL = "https://korimart.github.io/UOS-12/updateLink.txt";
    private static String announcementURL = "https://korimart.github.io/UOS-12/announcement.txt";

    private MutableLiveData<String> updateLink = new MutableLiveData<>();
    private MutableLiveData<String> announcement = new MutableLiveData<>();

    public void fetchGithub(Context context){
        new Thread(() -> {
            fetchUpdate(context);
            fetchAnnouncement();
        }).start();
    }

    private void fetchAnnouncement(){
        String response = null;
        try {
            response = WebService.INSTANCE.sendGet(announcementURL, "UTF-8");
        } catch (Exception e) {
            return;
        }
        announcement.postValue(response);
    }

    private void fetchUpdate(Context context){
        int verCode = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            verCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        String response;
        try {
            response = WebService.INSTANCE.sendGet(updateInfoURL, "UTF-8");
        } catch (Exception e) {
            return;
        }

        if (verCode < Integer.parseInt(response.trim())){
            try {
                String updateLink = WebService.INSTANCE.sendGet(updateLinkURL, "UTF-8");
                this.updateLink.postValue(updateLink.trim());
            } catch (Exception e) {
            }
        }
    }

    public MutableLiveData<String> getAnnouncement() {
        return announcement;
    }

    public MutableLiveData<String> getUpdateLink() {
        return updateLink;
    }
}
