package com.bureau.bureauapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.*;

import com.bureau.bureauapp.SplashScreen;
import com.bureau.bureauapp.accounts.ConfigurationActivity;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.digits.sdk.android.Digits;
import com.layer.sdk.LayerClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class Util {
    public static String streamToString(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    public static void invalidateUserID(Context context, Activity activity) {
        AppData.clearPreferences(context);
        //                    clear Digits Session
        Digits.clearActiveSession();
        deauthenticateLayer();

//                    clear All the User Data
        AppData.clearPreferences(context);

        activity.finishAffinity();
        Intent intent = new Intent(context, SplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private static void deauthenticateLayer() {
        MyApplication.deauthenticate(new com.layer.atlas.util.Util.DeauthenticationCallback() {
            @Override
            public void onDeauthenticationSuccess(LayerClient client) {
//                MyApplication.routeLogin(ConfigurationActivity.this);
                android.util.Log.e("ConfigurationActivity", "deauthenticateLayer success >>> ");
            }

            @Override
            public void onDeauthenticationFailed(LayerClient client, String reason) {
                android.util.Log.e("ConfigurationActivity", "reason >>> " + reason);
            }
        });
    }
}
