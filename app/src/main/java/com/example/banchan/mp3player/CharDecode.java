package com.example.banchan.mp3player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class CharDecode {

    public static String decodeFromFile (String mPath) {

        FileInputStream FS;
        byte[] mByte = new byte[6000];

        try{
            //  バイト読み込み
            FS = new FileInputStream(mPath);
            FS.read(mByte, 0, 6000);
            FS.close();
        }catch(FileNotFoundException e){
            return null;
        }catch (IOException e){
            return null;
        }

        String aa0 =  decodeFromByte(mByte);
        String aaa = aa0.toString().replaceAll("\\[[\\w\\.\\-: ']*\\]", "");
        String LINE_SEPARATOR_PATTERN =  "\r\n|[\n\r\u2028\u2029\u0085]";
        String[] aa1 = aaa.split(LINE_SEPARATOR_PATTERN, 0);
        String rtn1 = "";
        for(int k=0; k < aa1.length; k++){
            if(aa1[k].length() > 0) {
                rtn1 += aa1[k] + "\n";
            }
        }
        return rtn1;
    }

    public static String decodeFromByte(byte[] mByte) {
        String[] sCharset = {
                "UTF-8",
                "windows-31j",
                "EUC-JP",
                "ISO-2022-JP",
                "UTF-16"
        };

        CharsetDecoder decoder;
        CharBuffer Cbuffer = null;
        CoderResult result;
        ByteBuffer Bbuffer;

        Bbuffer = ByteBuffer.wrap(mByte);
        int i;

        for(i = 0; i < sCharset.length; i++){
            if( Charset.isSupported(sCharset[i]) ){
                decoder = Charset.forName(sCharset[i]).newDecoder();    //  元の文字コードを設定
                Cbuffer = CharBuffer.allocate(6000);   //  容量確保
                Cbuffer.position(0);
                //  default(UTF-8)に変換
                    result = decoder.decode(Bbuffer, Cbuffer, true);    //  trueは追加無し
 /*
                Log.d("■", "remaining " + sCharset[i]
                        + "\nisError " + result.isError()
                        + "\nisOverflow " + result.isOverflow()
                        + "\nisUnderflow " + result.isUnderflow()
                        + "\nisUnmappable " + result.isUnmappable()
                        + "\nisMalformed " + result.isMalformed());
*/
                if( ! result.isError() ){  //  変換が成功したら抜ける
                    break;
                }
                Bbuffer.rewind();   //  位置を戻す
            }
        }

        String mStr;
        StringBuilder rtn = new StringBuilder();
        if(i >= sCharset.length){   //  カウンタオーバーは失敗
            return "fault ...";
        }
        else{
            Cbuffer.flip();
            mStr = Cbuffer.toString();
            for(int j=0; j < mStr.length(); j++){
                int iCode;
                char mchar;
                mchar = mStr.charAt(j);
                iCode = mchar;

                if(( ! (j == 0 && (iCode == 0xfeff || iCode == 0xfffe )) )
                        && (iCode != 0x0000)){
                    rtn.append(mchar);
                }
            }
        }
        return rtn.toString();
    }
}
