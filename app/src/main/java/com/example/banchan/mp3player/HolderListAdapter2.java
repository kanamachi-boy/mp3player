package com.example.banchan.mp3player;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class HolderListAdapter2 extends BaseAdapter {
    Context mContext;
    LayoutInflater layoutInflater = null;
    ArrayList<SongHolder> songHolders;
    //private String pathNowPlaying = "";
    //private String dirNowPlaying = "";

    //private int playingHolderPosition = 0;

    public HolderListAdapter2(Context context, ArrayList<SongHolder> mItem) {
        mContext = context;
        songHolders = new ArrayList<SongHolder>();
        songHolders.addAll(mItem);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return songHolders.size();
    }

    @Override
    public Object getItem(int position) {
        return songHolders.get(position);
    }
/*
    public String getPlayingPath(int position){
        if((position >= songHolders.size())||(position < 0)){
            //  引数が負、または上限オーバー
            // ⇒ 引数positionを無視してplayingHolderPositionを進める
            playingHolderPosition++;
            if(playingHolderPosition >= songHolders.size()){
                playingHolderPosition = 0;
            }
        }
        else{
            playingHolderPosition = position;
        }
        //  リスト選択時に呼ばれるのでこのpositionを記録しておく
        //Log.d("■", "playingHolderPosition　" + playingHolderPosition);
        return songHolders.get(playingHolderPosition).path;
    }

    public String getPlayingDirName(){
        return songHolders.get(playingHolderPosition).name;
    }
*/
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){   //  viewHolderでviewの再利用
            convertView = layoutInflater.inflate(R.layout.inbox_1row, parent, false);
            holder = new ViewHolder();
            holder.vDir =  (TextView) convertView.findViewById(R.id.textView1);
            holder.vSongs = (TextView) convertView.findViewById(R.id.textView2);
            holder.vLoop = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }


        //  ディレクトリ名(title)
        holder.vDir.setText(songHolders.get(position).name);
        //  曲リスト（演奏中はハイライト、loop中ならアンダーライン）
        holder.setSongText(position);


        return convertView;
    }

    class ViewHolder{
        //  viewHolderに表示methodも実装
        TextView vDir;
        TextView vSongs;
        ImageView vLoop;

        public void setSongText(int mPos){
            //
            String mList = "";
            for(int i = 0; i < songHolders.get(mPos).song.size(); i++){
                String mItem = songHolders.get(mPos).song.get(i)[1];
                mList += mItem;
                if(i != songHolders.get(mPos).song.size() - 1){
                    mList += " / ";
                }
            }
            vSongs.setText(Html.fromHtml(mList));
        }
        /*
        public void switchLoop(int mPos){
            //  ループ表示
            if(isThisHolderLooping && playingHolderPosition == mPos){
                vLoop.setVisibility(View.VISIBLE);
            }
            else{
                vLoop.setVisibility(View.INVISIBLE);
            }
        }
        */
    }
/*
    public void setPathNowPlaying(String mPath){
        pathNowPlaying = mPath;
        if( ! mPath.equals("")){
            File file = new File(mPath);
            dirNowPlaying = file.getParent();
        }
        else{
            dirNowPlaying = "";
        }
        //Log.d("■", "dirNowPlaying　" +dirNowPlaying + " pathNowPlaying " + pathNowPlaying);
        //  曲が変わったら一曲ループは解除
        isThisTuneLooping = false;
    }
*/


}
