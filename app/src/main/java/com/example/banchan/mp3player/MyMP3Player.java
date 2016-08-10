package com.example.banchan.mp3player;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;

public class MyMP3Player {

    public interface SongChangeListener extends EventListener{
        //  作成するイベント
        void songChanged(String sPath);
    }

    private ArrayList<SongCue> sCue;
    private int sPointer;
    private int sLoopMode;
    private MediaPlayer sPlayer;
    private Context mContext;
    private SongChangeNotify SCNotify;

    MyMP3Player(Context context, SongChangeListener aListner){
        //  DBから、sCueへデータ格納
        mContext = context;
        DatabaseHelper DBH = new DatabaseHelper(mContext);
        ArrayList<String[]> aaa = new ArrayList<String[]>();
        String mBunrui = Constants.getPrefrenceString(mContext, Constants.BUNRUI, "0");
        aaa.addAll(DBH.getSongList(mBunrui));
        sCue = new ArrayList<SongCue>();
        for(int i = 0; i < aaa.size(); i++){
            SongCue SC = new SongCue();
            SC.title = aaa.get(i)[0];
            SC.path = aaa.get(i)[1];
            SC.holderPath = aaa.get(i)[2];
            sCue.add(SC);
        }

        sPointer = 0;
        sLoopMode = 0;

        //  イベントを発生させるクラスを生成。
        SCNotify = new SongChangeNotify();
        //  イベントを受け取るのはMainActivityなのでコンストラクタの第二引数を渡す。
        SCNotify.setListener(aListner);
    }


    private void innerPlay(final int mPointer){
        //  MainActivityへ曲変移を知らせる
        //  ⇒表示リスト（ハイライト付き）までこちらで用意するのは変だが、
        SCNotify.informSongChanged(sCue.get(mPointer).path);

        //  ポインタ位置の曲をplay
        try {
            if(sPlayer == null) {
                sPlayer = new MediaPlayer();
            }
            else{
                sPlayer.stop();
                sPlayer.release();
                sPlayer = null;
                sPlayer = new MediaPlayer();
            }
            sPlayer.setDataSource(getPath(mPointer));
            sPlayer.prepare();
        } catch (IOException e) {
            return;
        }
        sPlayer.start();
        //  ループモードを判定して再帰呼出しする
        sPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (sLoopMode == 2) { //  ホルダーループ
                    sPointer = getNextPositionAtHolderLooping(1);
                } else if (sLoopMode == 0) {   //  通常モード
                    sPointer = getNextPosition(1);
                }
                innerPlay(sPointer);    //  一曲ループはポインタ変えず
            }

        });
    }

    public void playOrPause(boolean isPlay){
        if(sPlayer != null){
            if(isPlay){
                sPlayer.pause();
            }
            else {
                sPlayer.start();
            }
        }
    }

    public void ffOrRewind(int mMode){
        if(sPlayer != null && sPlayer.isPlaying()){
            int aMode = mMode > 0 ? 1 : -1;
            int mMilsec = sPlayer.getCurrentPosition() +
                    aMode * (int)(Constants.getPrefrenceLong(mContext, Constants.FF_RW_MILLSEC, 5000));
            //Log.d("■", "" + mp.getDuration());
            sPlayer.seekTo(mMilsec);
            sPlayer.start();
        }
    }

    public void prevOrNext(int mMode){

        if(sPlayer != null && sPlayer.isPlaying()){
            if(mMode == -1){      //  -1は前曲
                if(sLoopMode == 2){
                    sPointer = getNextPositionAtHolderLooping(-1);
                }
                else{
                    sPointer = getNextPosition(-1);
                }
            }
            else{
                if(sLoopMode == 2){
                    sPointer = getNextPositionAtHolderLooping(1);
                }
                else{
                    sPointer = getNextPosition(1);
                }
            }
        }
        innerPlay(sPointer);
    }

    public void stop(){
        sLoopMode = 0;
        if (sPlayer != null) {
            sPlayer.stop();
            sPlayer.reset();
            sPlayer.release();
            sPlayer = null;
        }
    }

    public int rotateLoopMode(){
        //   ループ設定　ホルダー内１周:0 ⇒ 一曲ループ:1 ⇒ ホルダー内ループ:2 ⇒ 戻る
        sLoopMode ++;
        if (sLoopMode > 2){
            sLoopMode =0;
        }
        return sLoopMode;
    }

    public boolean play(String aHolderPath){
        //  指定ホルダー先頭曲のポインタを探してinnerPlayへ渡す
        for(int i = 0; i < sCue.size(); i++){
            if(sCue.get(i).holderPath.equals(aHolderPath)){
                sPointer = i;
                //sLoopMode = 0;
                innerPlay(sPointer);
                return true;
            }
        }
        return false;
    }

    private class SongCue{
        String title;
        String path;
        String holderPath;
    }

    private String getPath(int aPointer){
        //  ポインタ位置のパスを返す
        return sCue.get(aPointer).path;
    }

    private int getNextPositionAtHolderLooping(int aDirection){

        int[] holderRange = getCurrentHolderRange();

        //  正逆それぞれポインタを設定
        int nextP;
        if(aDirection == 1) {   //  正方向
            nextP = sPointer + 1;
            if (nextP > holderRange[1]) {
                nextP = holderRange[0];
            }
        }
        else{
            nextP = sPointer - 1;
            if (nextP < holderRange[0]) {
                nextP = holderRange[1];
            }
        }
        return nextP;
    }

    private int getNextPosition(int aDirection){
        //  正逆それぞれポインタを設定
        int nextP;
        if(aDirection == 1) {   //  正方向
            nextP = sPointer + 1;
            if (nextP > sCue.size() - 1) {
                nextP = 0;
            }
        }
        else{
            nextP = sPointer - 1;
            if (nextP < 0) {
                nextP = sCue.size() - 1;
            }
        }
        return nextP;
    }

    private int[] getCurrentHolderRange(){
        String path1 = sCue.get(sPointer).holderPath;
        //  現行ホルダーの範囲を調べる
        ArrayList<Integer> holderRange = new ArrayList<Integer>();
        for(int i = 0; i < sCue.size(); i++){
            if(sCue.get(i).holderPath.equals(path1)){
                holderRange.add(i);
            }
        }
        int[] rtn = {holderRange.get(0), holderRange.get(holderRange.size() - 1 )};
        return rtn;
    }

    private class SongChangeNotify {

        private SongChangeListener SCListener;

        //  これを叩くとIFで定義したsingChangedを発生させる
        public void informSongChanged(String sPath){
            SCListener.songChanged(sPath);
        }

        //  イベントの受け取り先をセットする。この場合はMainActivity。
        //  MyMP3PlayerのコンストラクタにSongChangeListener
        //  をimplementしたthisを渡し、MyMP3Player内でセット
        public void setListener(SongChangeListener aListener){
            this.SCListener = aListener;
        }

    }

}
