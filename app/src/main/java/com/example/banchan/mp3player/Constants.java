package com.example.banchan.mp3player;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

////    カスタム定数定義クラス

public class Constants {

    public static final String LAST_ADDED_DATE ="lastaddeddate";
    public static final String LAST_SONGS_QTY ="lastsongsqty";
    public static final String BUNRUI ="bunruicode";
    public static final String SHOW_LYRIC ="showlyric";
    public static final String FF_RW_MILLSEC ="ffrwmillsec";
    public static final String SONG_JUMP_QTY ="songjumpqty";

    static void setPrefrenceString(Context context, String mKey, String mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putString(mKey, mVal);
        editor.apply();
    }

    static String getPrefrenceString(Context context, String mKey, String mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getString(mKey, mDefault);
    }

    static void setPrefrenceLong(Context context, String mKey, long mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putLong(mKey, mVal);
        editor.apply();
    }

    static long getPrefrenceLong(Context context, String mKey, long mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getLong(mKey, mDefault);
    }

    static void setPrefrenceBoolean(Context context, String mKey, Boolean mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean(mKey, mVal);
        editor.apply();
    }

    static Boolean getPrefrenceBoolean(Context context, String mKey, Boolean mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getBoolean(mKey, mDefault);
    }

/*
    static void setPrefrenceFloat(Context context, String mKey, Float mVal){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = data.edit();
        editor.putFloat(mKey, mVal);
        editor.apply();
    }

    static Float getPrefrenceFloat(Context context, String mKey, Float mDefault){
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
        return data.getFloat(mKey, mDefault);
    }

    */

}
