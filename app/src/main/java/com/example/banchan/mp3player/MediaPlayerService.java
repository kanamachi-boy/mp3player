package com.example.banchan.mp3player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MediaPlayerService extends Service
        implements MyMP3Player.SongChangeListener {

    private Messenger _messenger;
    private Messenger _reply;
    private MyMP3Player mMP3;

    @Override
    public void songChanged(String sPath) {
        if(_reply != null){
            try {
                _reply.send(Message.obtain(null, 2, sPath));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    class TestHandler extends Handler{
        private Context _context;
        public TestHandler(Context context){
            _context = context;
        }
        @Override
        public void handleMessage(Message msg){
            _reply = msg.replyTo;
            Bundle arg = msg.getData();
            switch (msg.what){
                case 0:
                    //  startPlayHolder
                    String aData = arg.getString("MyMsg");
                    mMP3.play(aData);

                    break;
                case 1:

                    break;
                case 2:
                    //  rotateLoopMode
                    int aLoop = mMP3.rotateLoopMode();
                    if(_reply != null){
                        try {
                            _reply.send(Message.obtain(null, 0, aLoop));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case 3:
                    //  prevOrNext
                    int aMode = arg.getString("MyMsg").equals("1") ? 1 : -1;
                    mMP3.prevOrNext(aMode);
                    break;
                case 4:
                    //  ffOrRewind
                    int aMode2 = arg.getString("MyMsg").equals("1") ? 1 : -1;
                    mMP3.ffOrRewind(aMode2);
                    break;
                case 5:
                    //  playOrPause
                    boolean aPlay = arg.getString("MyMsg").equals("1") ? true : false;
                    mMP3.playOrPause(aPlay);
                    break;
                default:
                    //  stop
                    mMP3.stop();

            }




        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _messenger = new Messenger(new TestHandler(getApplicationContext()));

        mMP3 = new MyMP3Player(getApplicationContext(), this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return _messenger.getBinder();

    }
    @Override
    public void onDestroy() {
    }

}
