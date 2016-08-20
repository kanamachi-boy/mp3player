package com.example.banchan.mp3player;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
        implements ServiceConnection {

    private ListView lv1;
    private HolderListAdapter2 HLAdp;
    //private MyMP3Player mMP3;
    private Messenger _messenger;
    private Messenger _replyMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isContentAdded()){
            Toast.makeText(this, "DBを構築しています。", Toast.LENGTH_SHORT).show();
            DatabaseHelper DBH = new DatabaseHelper(this);
            DBH.initialSetting();
            DBH.close();
        }

        //  MP3をラップしたサービスに接続
        bindService(new Intent(this, MediaPlayerService.class), this, Context.BIND_AUTO_CREATE);

        lv1 = (ListView)findViewById(R.id.lv1);
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //startPlayHolder(position);
                sendMsgToService(HLAdp.songHolders.get(position).path, 0);
                dispLyric();
            }
        });
        setListView();
        //  スリープ制御
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        _messenger = new Messenger(service);
        _replyMessenger = new Messenger(new ReplyHandler(this));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        finishOrDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId()){
            case R.id.menu_setting:
                Intent intent = new Intent(this, SettingFGActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_end:
                //
                finishOrDestroy();
                break;
        }
        return super.onOptionsItemSelected(item);
    }   //  option

   private boolean isContentAdded() {
        //  ****************************
        /////   データ更新されたかチェック  最新追加DATE + 総曲数
        long mLastAdded = Constants.getPrefrenceLong(this, Constants.LAST_ADDED_DATE, 0);
        long mLastQty = Constants.getPrefrenceLong(this, Constants.LAST_SONGS_QTY, 0);

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = getContentResolver();
        String[] columns = { MediaStore.Audio.Media.DATE_ADDED };
        String mOrder = "date_added DESC";
        Cursor cur;
        Boolean result;
        cur = resolver.query(uri, columns, null, null, mOrder);
        if (cur.moveToFirst()) {
            if(cur.getLong(0) > mLastAdded || mLastQty != cur.getCount()){
                Constants.setPrefrenceLong(this, Constants.LAST_ADDED_DATE, cur.getLong(0));
                Constants.setPrefrenceLong(this, Constants.LAST_SONGS_QTY, (long)cur.getCount());
                result = true;
            } else{
                result = false;
            }
        }
        else{
            result = false;
        }
        cur.close();
        return result;
    }

    public void audioStop(){
        sendMsgToService("", 9);
        closeLyrics();
    }

    public void dispLyric(){
        //  すでに開いていたらそのまま
        FragmentManager manager = getSupportFragmentManager();
        Fragment fm = (Fragment)manager.findFragmentByTag("LY");
        if(fm == null) {
            Lylics LY = new Lylics();
            manager.beginTransaction()
                    //.addToBackStack("Lylics")
                    .replace(R.id.contener1, LY, "LY")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    public void closeLyrics(){
        FragmentManager manager = getSupportFragmentManager();
        Fragment fm = (Fragment)manager.findFragmentByTag("LY");
        if(fm != null) {
            manager.beginTransaction()
                    .remove(fm)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        }
    }

    public void playOrPause(boolean isPlay){
        String aPlay = isPlay ? "1" : "0";
        sendMsgToService(aPlay, 5);
    }

    public void ffOrRewind(int mMode){
        String aMode = mMode == 1 ? "1" : "-1";
        sendMsgToService(aMode, 4);
    }

    public void prevOrNext(int mMode){
        String aMode = mMode == 1 ? "1" : "-1";
        sendMsgToService(aMode, 3);
    }

    public int rotateLoopMode(){
        //   ループ設定　ホルダー内１周:0 ⇒ 一曲ループ:1 ⇒ ホルダー内ループ:2 ⇒ 戻る
        sendMsgToService("", 2);
        return 0;
    }

    public void notifyLyric(String aArg){
        //  （最初にfragmentを生成した時）操作できるまでループして待つ
        //  bundleでは初回しか渡せないのでこのmethodを使う

        String aa0[] = aArg.split("/", 0);
        final String mTitle = aa0[aa0.length - 2];
        String aa[] = aArg.split("\\.", 0);
        String mLiric0 = CharDecode.decodeFromFile(aa[0] + ".lrc");
        if(mLiric0 == null || mLiric0.equals("")){
            mLiric0 = CharDecode.decodeFromFile(aa[0] + ".txt");
        }
        final String mLyric = mLiric0;
        final String mList = createSongList(aArg);

        new Thread(){
            Fragment mFrg;
            public void run(){
                //
                int cnt = 0;
                do{
                    mFrg = getSupportFragmentManager().findFragmentByTag("LY");
                    try {
                        Thread.sleep(100);  //  初回は、大体2回目で掴める
                    } catch (InterruptedException e) {
                        return; //  エラーを起こしたら諦める
                    }
                    cnt ++;
                    if(cnt > 30){   //  3秒待ったら諦める
                        return;
                    }
                }while (mFrg == null || !(mFrg instanceof Lylics));

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //  fragmentのインスタンスが掴めたら曲名等をセット
                        ((Lylics) mFrg).changeSong(mTitle, mLyric, mList);
                    }
                });
            }
        }.start();
    }

    public void setListView(){
        final ProgressDialog progressdialog = new ProgressDialog(MainActivity.this);
        progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressdialog.setMessage("曲名リストを用意しています");
        progressdialog.show();
        final DatabaseHelper DBH = new DatabaseHelper(this);

        new Thread(){
            public void run(){
                //
                HLAdp = new HolderListAdapter2(getApplicationContext(), DBH.getHolderData(
                        Constants.getPrefrenceString(getApplicationContext(), Constants.BUNRUI, "0")));
                DBH.close();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //
                        lv1.setAdapter(HLAdp);
                        progressdialog.dismiss();
                    }
                });
            }
        }.start();
    }

    public class ReplyHandler extends Handler {
        private Context _cont;
        public ReplyHandler(Context cont) {
            _cont = cont;
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    //  ループモード変移
                    Fragment mFrg = getSupportFragmentManager().findFragmentByTag("LY");
                    ((Lylics) mFrg).changeLoopIcon((Integer)msg.obj);
                    break;
                case 2:
                    //  曲変移
                    notifyLyric((String)msg.obj);
                break;
                default:
            }
        }
    }

    private String createSongList(String aPath){
        //  ディレクトリを取り出す
        String aaa[] = aPath.split("/", 0);
        String dPath = "";
        for(int i = 0; i <= aaa.length - 2; i++){
            dPath += aaa[i];
            if(i != aaa.length - 2){
                dPath += "/";
            }
        }
        //
        for(int j = 0; j < HLAdp.songHolders.size(); j++){
            if(HLAdp.songHolders.get(j).path.equals(dPath)){
                String mList = "";
                for(int k = 0; k < HLAdp.songHolders.get(j).song.size(); k++){
                    String mItem = HLAdp.songHolders.get(j).song.get(k)[1];
                    String tabA = "";
                    String tabB = "";
                    if (HLAdp.songHolders.get(j).song.get(k)[0].equals(aPath)){
                        tabA = "<font color=green><big>";
                        tabB = "</big></font>";
                    }
                    else{
                        tabA = "<small>";
                        tabB = "</small>";
                    }
                    mList += tabA +  mItem + tabB;
                    if(k != HLAdp.songHolders.get(j).song.size() - 1){
                        mList += " / ";
                    }
                }
                return mList;
            }
        }
        return null;
    }

    public void sendMsgToService(String aMsg, int aID){
        Bundle arg = new Bundle();
        arg.putString("MyMsg", aMsg);
        Message msg = Message.obtain(null, aID);
        msg.setData(arg);
        msg.replyTo = _replyMessenger;
        try {
            _messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void finishOrDestroy(){
        //  スリープ制御の解除
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try{
            audioStop();
            unbindService(this);
            _messenger = null;
            _replyMessenger = null;
        }catch (Exception e){
            Log.d("■", "onDestroy " + e.getMessage());
        }
        finish();
    }

}
