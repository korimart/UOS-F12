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

    private MutableLiveData<String> updateLink = new MutableLiveData<>();
    private MutableLiveData<Boolean> noPnp = new MutableLiveData<>();

    public void fetchUpdateInfo(Context context){
        new Thread(() -> checkUpdate(context)).start();
    }

    private void checkUpdate(Context context){
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

        if (verCode < Integer.parseInt(response)){
            try {
                String updateLink = WebService.INSTANCE.sendGet(updateLinkURL, "UTF-8");
                this.updateLink.postValue(updateLink);
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
            noPnp.postValue(false);
            writeNoPnp(context);
        }
    }

    public void writeNoPnp(Context context){
        try {
            OutputStreamWriter osw = new OutputStreamWriter(
                    context.openFileOutput("settings.txt", Context.MODE_PRIVATE));
            String setting = noPnp.getValue() ? "noPnp\n" : "pnp\n";
            osw.write(setting);
            osw.flush();
            osw.close();
        } catch (IOException ignore) {
        }
    }

    public MutableLiveData<String> getUpdateLink() {
        return updateLink;
    }

    public MutableLiveData<Boolean> getNoPnp() {
        return noPnp;
    }
}