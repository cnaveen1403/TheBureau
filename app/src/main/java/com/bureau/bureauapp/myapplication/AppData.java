package com.bureau.bureauapp.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class AppData {

    private static final String SQLITE_NAME = "TheBureau";

    // for saving and geeting the result....
    public static boolean saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SQLITE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SQLITE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static boolean saveInteger(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SQLITE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getInteger(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SQLITE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public static boolean saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SQLITE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SQLITE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public static boolean containsKey(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SQLITE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }

    public static void clearPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SQLITE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
//        editor.apply();
        editor.commit();
    }
}
