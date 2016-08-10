package com.example.banchan.mp3player;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mp3TagManager {

    private int mVersion;   //  (2).4ならsynchsafe型で計算
                                            //  (2).2ならフレームヘッダが6バイト
    private int mSize;  //  調べるサイズ（ヘッダの大きさ）
    private byte[] mTagData;    //  タグ部分のバイトデータ

    Mp3TagManager(String mPath) {
        //  ID3.1は対象外（ファイル末にタグ）mVersionが0のまま
        mVersion = 0;
        mSize = 0;
        //  mpファイルを10バイト読み込む
        byte[] mByte = readFileByte(mPath, 0, 10);
        //　ヘッダ選別子確認
        byte[] mID = Arrays.copyOfRange(mByte, 0, 3);
        String aa = null;
        try {
            aa = new String(mID, "UTF-8");
            if ( ! aa.equals("ID3")){
                return; //  mVersionが0のままになる
            }
        } catch (UnsupportedEncodingException e) {
            return;
        }

        //  バージョン確認
        byte[] mVer = Arrays.copyOfRange(mByte, 3, 4);
        mVersion = (int) mVer[0];

        //  拡張ヘッダ有無確認
        byte[] mFlg = Arrays.copyOfRange(mByte, 5, 6);
        BitSet bitSet = BitSet.valueOf(mFlg);
        boolean opHead = bitSet.get(6);

        //String hd = Integer.toBinaryString(mFlg[0]).replace(' ', '0');

        //  タグサイズ確認⇒サイズなので位置を指定する時は-1
        for(int i = 6; i <= 9; i++){
            byte[] m1 = Arrays.copyOfRange(mByte, i, i + 1);
            double b1 = Math.pow(128, (9 - i)) ;
            mSize += b1 * ( m1[0] );
        }
        if(opHead){ //  拡張ヘッダがあれば+6
            mSize += 16;
        }
        else{
            mSize += 10;
        }

        //  タグ部分を読み込んで保持する。
        mTagData = readFileByte(mPath, 0, mSize);

        //Log.d("■", aa + " " + mVersion + " " + opHead + " " +  mSize + " " + String.format("%04X", mSize));
    }

    private byte[] readFileByte(String mPath, int mOff, int mLen){
        byte[] aByte = new byte[mLen];
        try {
            FileInputStream fis = new FileInputStream(mPath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            fis.read(aByte, mOff, mLen);
            baos.write(aByte);
            /*
                while(fis.read(aByte) != -1) {
                    baos.write(aByte);

                }
            */
                baos.close();
                fis.close();
            aByte = baos.toByteArray();
            } catch (IOException e1) {
                e1.printStackTrace();

        }

        return aByte;
    }

    public String getFrameHeaders(){

        if(mVersion == 0){
            return "unknown tag format";
        }

        //  フレームヘッダを次々GETしてゆく
        String rtn = "";
        int mPointer = 10;  //  初期値は10
        //  v2.2はフレームヘッダが6バイト
        int mIDsize = ( mVersion == 2 ) ? 3 : 4;
        int mSizeLength = ( mVersion == 2 ) ? 3 :4;
        int mFlgSize = ( mVersion == 2 ) ? 0 :2;

        rtn += "ver2." + mVersion + "\n";

        //   10バイト目から始める
        do {
            //byte[] mID = Arrays.copyOfRange(mTagData, mPointer, mIDsize);
            byte[] mID = Arrays.copyOfRange(mTagData, mPointer, mPointer + mIDsize);
            String aa = null;
            try {
                aa = new String(mID, "UTF-8");
                //  *はnull？とマッチしてしまうのでダメ。+を使う
                Pattern pattern = Pattern.compile("[A-Z0-9]+");
                Matcher matcher = pattern.matcher(aa);
                if(matcher.find()){
                    //  タグの有効部分終端チェック
                    //Log.d("■", "" + aa);
                    rtn += aa + " : ";
                }
                else{
                    return rtn;
                }


            } catch (UnsupportedEncodingException e) {
                return null;
            }

            //  フレームヘッダを読んでIDをリストに格納
            //  （ver2.2はヘッダが6バイト）


            //  フレームサイズを調べて次のヘッダ位置を決める
            //  （ver2.4はSyncsafe型）
            mPointer += mIDsize;
            byte[] mSize = Arrays.copyOfRange(mTagData, mPointer, mPointer + mSizeLength);
            int fSize =0;
            if(mVersion == 4) {
                for(int i = 0; i <= mSize.length; i++){
                    double b1 = Math.pow(128, (9 - i)) ;
                    fSize += b1 * ( mSize[i] );
                }
            }
            else{
                ByteBuffer buf = ByteBuffer.wrap(mSize);
                fSize = buf.getInt();
            }

            mPointer += mSizeLength + mFlgSize;
            byte[] mCode = Arrays.copyOfRange(mTagData, mPointer, mPointer + 1 );
            rtn += (int)mCode[0] + " : ";

            mPointer += 1;
            byte[] mTxt = Arrays.copyOfRange(mTagData, mPointer, mPointer + fSize -1 );

            //if(aa.equals("USLT")){
                //  歌詞の先頭にeng(BOM)が付いている場合あり⇒理由不明
                byte[] mTxtTop = Arrays.copyOfRange(mTxt, 0, 7);
                String mTop = bytes2Hex(mTxtTop);
                Pattern p1 = Pattern.compile("([0-9A-F]+)FFFE0000");
                Matcher mch = p1.matcher(mTop);
                if(mch.find()){
                    rtn += CharDecode.decodeFromByte(Arrays.copyOfRange(mTxtTop, 0, 3)) + " : ";
                    //rtn += mch.group(1) + " : ";
                    //if(mTop.equals("656E67FFFE0000")){
                    mTxt = Arrays.copyOfRange(mTxt, 7, 7 + fSize );

                }
            //}


            String aTxt = CharDecode.decodeFromByte(mTxt) + "\n";
            //Log.d("■", "" + aTxt);
            rtn += aTxt + "\n";
            mPointer += fSize -1 ;

        }while (mPointer < mSize);

        //  タグ範囲に達したら終了

        return rtn;
    }

    static String bytes2Hex(byte[] args) {
        String rtn = "";
        for (int i = 0; i < args.length; i++) {
            rtn += String.format("%02X", args[i]);
        }
        return rtn;
    }

}
