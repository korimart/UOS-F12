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
    private MutableLiveData<Boolean> noPnp = new MutableLiveData<>();

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

    public void fetchNoPnp(Context context){
        File internalPath = context.getFilesDir();
        Path settingsPath = Paths.get(internalPath.getPath(), "settings.txt");
        if (settingsPath.toFile().isFile()){
            try {
                List<String> settings = Files.readAllLines(settingsPath);
                noPnp.postValue(settings.get(0).equals("noPnp"));
            } catch (IOException ignore) {
            }
        }
        else {
            writeNoPnp(context);
        }
    }

    public void writeNoPnp(Context context){
        try {
            OutputStreamWriter osw = new OutputStreamWriter(
                    context.openFileOutput("settings.txt", Context.MODE_PRIVATE));
            Boolean noPnpVal = noPnp.getValue();
            boolean noPnpPrim = false;
            if (noPnpVal != null) noPnpPrim = noPnpVal;

            String setting = noPnpPrim ? "noPnp\n" : "pnp\n";
            osw.write(setting);
            osw.flush();
            osw.close();
        } catch (IOException ignore) {
        }
    }

    public MutableLiveData<String> getAnnouncement() {
        return announcement;
    }

    public MutableLiveData<String> getUpdateLink() {
        return updateLink;
    }

    public MutableLiveData<Boolean> getNoPnp() {
        return noPnp;
    }
}
