package com.korimart.f12;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.function.Consumer;

public class UpdateChecker {
    private static String updateInfoURL = "https://korimart.github.io/UOS-12/updateInfo.txt";
    private static String updateLinkURL = "https://korimart.github.io/UOS-12/updateLink.txt";

    // returns url if needs update
    public static void checkUpdate(Context context, Consumer<String> callback){
        int verCode = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            verCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            callback.accept(null);
            return;
        }

        String response;
        try {
            response = WebService.sendGet(updateInfoURL, "UTF-8");
        } catch (Exception e) {
            callback.accept(null);
            return;
        }

        if (verCode < Integer.parseInt(response)){
            try {
                String updateLink = WebService.sendGet(updateLinkURL, "UTF-8");
                callback.accept(updateLink);
            } catch (Exception e) {
                callback.accept(null);
            }
        }
    }
}
