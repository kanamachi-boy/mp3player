package com.example.banchan.mp3player;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

public class SettingFGActivity extends Activity {

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  fragmentの作成
        makePrefFragment();
        //  「 < 」をホームへ戻るように設定
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void makePrefFragment(){

        //  fragmentへ値を渡したければbundleを使う
        Bundle bundle = new Bundle();
        //bundle.putString("last_code", Constants.getPrefrenceString(this, Constants.MY_AREA_CODE, "130010"));

        //  継承クラスを作成して表示する
        PrefFragment pF = new PrefFragment();
        pF.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, pF)
                .commit();

    }

    public static class PrefFragment extends PreferenceFragment{
        //  継承クラス
        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //  xmlを読み込む
            addPreferencesFromResource(R.xml.pref_settings);

            //  各項目の設定値をSummaryに表示する
            ListPreference lp3 =(ListPreference) findPreference(Constants.BUNRUI);
            lp3.setSummary(lp3.getEntry());
            //  設定値をそのまま表示するなら .getValue()

        }

        @Override
        public void onStart(){
            super.onStart();
            //   設定変更を監視するリスナーを設定
            //  unreg...はonPause（画面を離れた時）されるのに対し
            //  onCreateは、終了せずに戻った時呼ばれないので機能しなくなる。で、こちらへ移動。
            PreferenceScreen root = getPreferenceScreen();
            root.getSharedPreferences().registerOnSharedPreferenceChangeListener(onPreferenceChangeListenter);

        }

        @Override
        public void onPause(){
            super.onPause();
            getPreferenceScreen().getSharedPreferences().
                    //  変更監視リスナーを解放
                    unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListenter);

        }

        private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListenter
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //  変更された時リストのsummaryに表示値を示す（保存値ではない）
                final Preference preference = getPreferenceScreen().findPreference(key);
                if(preference instanceof ListPreference){
                    final ListPreference listPreference = (ListPreference)preference;
                    listPreference.setSummary(listPreference.getEntry());

                }
            }
        };

    }

}
