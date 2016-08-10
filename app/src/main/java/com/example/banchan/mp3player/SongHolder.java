package com.example.banchan.mp3player;

import java.util.ArrayList;

public class SongHolder {

    public String name;
    public String path;
    public boolean isLoop;
    public boolean playable;
    public ArrayList<String[]> song;

    SongHolder(){
        //  内部配列を初期化しておかないとNPEになる！
        song = new ArrayList<String[]>();
    }

}
