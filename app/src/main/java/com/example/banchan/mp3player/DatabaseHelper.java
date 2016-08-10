package com.example.banchan.mp3player;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

//SQLite処理Helper
public class DatabaseHelper extends SQLiteOpenHelper{

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_FILE_NAME = "mp3player_0_3.db";
        private Context mContext;
        private SQLiteDatabase mDb;

        public DatabaseHelper(Context context) {
                super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
                mContext = context;
                mDb = this.getWritableDatabase();
            }     // コンストラクタ

        public void onCreate(SQLiteDatabase db) {
                //
                //  getWritableDatabase();の度にチェックされ
                //  DBが無い時（作成された時）だけ実行される。
                //  DB自体を作成するメソッドは無い！無ければ自動的に作成されるが
                //  それは最初のテーブルをcreteした時。
                //
            try {
                db.execSQL(
                        "CREATE TABLE song_table ("
                                + "_id integer,"  //  content providerのIDをcopyする    //  ④srot
                                + "path text not null, "    //  fullpath
                                + "holder text not null, "             //  ホルダー fullpath　  ①sort
                                + "title text, "  //  名前    def: title
                                + "artist text, "   //  アーティスト def: artist または空白
                                + "album_id integer, "                                          //  ②sort
                                + "track integer, "      // トラック番号 def: track または 0　　③sort
                                + "bunrui integer, "    //  music = 0  podcast = 1
                                + "playable  integer, "   //  再生対象 def: 1
                                + "lyrics text" //      歌詞　def:空白（ホルダー内にファイルがあれば読み込み？）
                                + ")"
                );
                /*
                db.execSQL(
                        "CREATE TABLE holder_table ("
                                + "holder text not null unique, "             // ホルダー　fullpath
                                + "alias text, "                    //  別名  def: 空白
                                + "name text not null, "  //  表示名   def: fullpath最後のエレメント
                                + "order integer, "            //   再生順   def: 所属songの最小_id順
                                + "playable integer, "      //  再生対象　def: 1
                                + "isloop  integer"             //  ホルダー内ループ def: 0
                                + ")"
                );
                */
            }catch (Exception e){
                Log.d("■", "onCreate DB " + e.getMessage());
            }
        }    // DB生成

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void initialSetting (){

        ContentResolver resolver = mContext.getContentResolver();
        //  uriはSQLで from xxx

        mDb.beginTransaction();
        try {
            //  一旦全て削除
            mDb.execSQL("delete from song_table");
            String[] columns = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.IS_MUSIC,
                    MediaStore.Audio.Media.IS_PODCAST
            };
            String where = MediaStore.Audio.Media.IS_MUSIC + " != ? " +
                    " or " +
                    MediaStore.Audio.Media.IS_PODCAST + " != ? ";
            String[] args = {"0", "0"};
            Cursor cur = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, where, args,
                    MediaStore.Audio.Media._ID);

                final SQLiteStatement stmt = mDb.compileStatement(
                        "INSERT INTO song_table" +
                                "( _id, path, holder, title, artist, album_id, track, bunrui, playable )" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1 )");
                try {
                    if (cur.moveToFirst()) {
                        do {
                            File mFile = new File(cur.getString(1));    //  path
                            if (mFile.isFile()) {
                                stmt.bindLong(1, cur.getInt(0));
                                stmt.bindString(2, cur.getString(1));
                                stmt.bindString(3, mFile.getParent());
                                stmt.bindString(4, cur.getString(2));   //  タイトル
                                stmt.bindString(5, cur.getString(3));   //  アーティスト
                                stmt.bindLong(6, cur.getInt(4));
                                stmt.bindLong(7, cur.getInt(5));

                                if(cur.getInt(6) != 0 ){
                                    stmt.bindLong(8, 0);
                                }
                                else{
                                    stmt.bindLong(8, 1);
                                }

                                stmt.executeInsert();
                            }

                        } while (cur.moveToNext());
                    }
                }finally {
                    stmt.close();
                }

            mDb.setTransactionSuccessful();
        }catch (Exception e){
            Log.d("■", "initialSetting　" + e.getMessage());
        }
        finally {
            mDb.endTransaction();
        }


    }

    public ArrayList<String[]> getSongList(String mBunrui){
        /////   MyMP3Player用のデータ
        try {
            String sql = "select title, path, holder from song_table where bunrui = ? " +
                    "order by holder, album_id, track, _id";
            String arg0 = mBunrui.equals("0") ? "0" : "1";  //  0以外はpodcast
            String[] args = {arg0};
            Cursor csr = mDb.rawQuery(sql, args);
            if(csr.getCount() != 0) {
                ArrayList<String[]> rtnUri = new ArrayList<String[]>();
                csr.moveToFirst();
                do {
                    String aa[] = {csr.getString(0), csr.getString(1), csr.getString(2)};
                    rtnUri.add(aa);
                } while (csr.moveToNext());
                csr.close();
                return rtnUri;
            } else{
                csr.close();
                return null;
            }
        }
        catch (Exception e){
            Log.d("■", "getSongList " + e.getMessage() );
            return null;
        }
    }

    public ArrayList<String[]> getSongListByHolder (String mHolder){
        /////   表示対象の品目iデータを取得

        try {
            String sql = "select path, title from song_table where holder = ? order by album_id, track, _id";
            String[] arg ={mHolder};
            Cursor csr = mDb.rawQuery(sql, arg);
            if(csr.getCount() != 0) {

                ArrayList<String[]> rtnUri = new ArrayList<String[]>();
                csr.moveToFirst();
                do {
                    String aa[] = {csr.getString(0) , csr.getString(1)};
                    rtnUri.add(aa);
                } while (csr.moveToNext());
                csr.close();
                return rtnUri;
            } else{
                csr.close();
                return null;
            }
        }
        catch (Exception e){
            Log.d("■", "getItemiData " + e.getMessage() );
            return null;
        }
    }

    public ArrayList<SongHolder> getHolderData (String mBunrui){
        /////   表示対象の品目iデータを取得

       try {
            String sql = "select holder, path, title from song_table where bunrui = ? order by " +
                    "holder, album_id, track, _id";
           String arg0 = mBunrui.equals("0") ? "0" : "1";  //  0以外はpodcast
            String[] args = {arg0};
            Cursor csr = mDb.rawQuery(sql, args);
            if(csr.getCount() != 0) {

                ArrayList<SongHolder> rtnUri = new ArrayList<SongHolder>();
                csr.moveToFirst();
                String thisHolder = csr.getString(0);   //  先頭レコードをKeyにセットする
                SongHolder mSH = new SongHolder();
                do {

                    if (! csr.getString(0).equals(thisHolder)){
                        //  holderが変わったら配列に追加し、項目を初期化する
                        rtnUri.add(mSH);
                        mSH = new SongHolder();
                    }

                    mSH.path = csr.getString(0);
                    String[] mPath = csr.getString(0).split("/", 0);
                    mSH.name = mPath[mPath.length -1 ];
                    mSH.isLoop = false;
                    mSH.playable = true;
                    String mSong[] = {csr.getString(1), csr.getString(2)};
                    mSH.song.add(mSong);
                    thisHolder = csr.getString(0);

                } while (csr.moveToNext());
                //  最後はここで追加処理
                rtnUri.add(mSH);

                csr.close();
                return rtnUri;
            }
            else{
                csr.close();
                return null;
            }

        }
        catch (Exception e){
            Log.d("■", "getItemiData " + e.getMessage() );
            return null;
        }

    }


}