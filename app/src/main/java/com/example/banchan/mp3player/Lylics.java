package com.example.banchan.mp3player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class Lylics extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private String mLyric;
    //private String mTitle;
    private boolean isPlay;
    //private Mp3TagManager MP3;
    TextView tv;
    TextView tv0;
    TextView tv1;
    ImageButton ib5;

/*
    public static Lylics newInstance(String param1, String param2) {
        Lylics fragment = new Lylics();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }
    */
    public Lylics() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_lylics, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //  mainActivityへのイベント遮断のため
                return true;
            }
        });

        tv = (TextView) view.findViewById(R.id.fgTxt);
        tv0 = (TextView) view.findViewById(R.id.lyric_title);
        tv1 = (TextView) view.findViewById(R.id.lyric_list);


        ImageButton ib1 = (ImageButton)view.findViewById(R.id.lyric_stop);
        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity MA = (MainActivity) getActivity();
                MA.audioStop();
                MA.closeLyrics();
            }
        });

        final ImageButton ib2 = (ImageButton)view.findViewById(R.id.lyric_pause);
        isPlay = true;
        ib2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity MA = (MainActivity) getActivity();
                MA.playOrPause(isPlay);
                if(isPlay){
                    ib2.setImageResource(R.drawable.ic_action_play);
                    isPlay = false;
                }
                else{
                    ib2.setImageResource(R.drawable.ic_action_pause);
                    isPlay = true;
                }
            }
        });

        ImageButton ib3 = (ImageButton)view.findViewById(R.id.lyric_ff);
        ib3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlay) {
                    MainActivity MA = (MainActivity) getActivity();
                    MA.ffOrRewind(1);
                }
            }
        });

        ImageButton ib4 = (ImageButton)view.findViewById(R.id.lyric_rw);
        ib4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlay) {
                    MainActivity MA = (MainActivity) getActivity();
                    MA.ffOrRewind(-1);
                }
            }
        });

        ib5 = (ImageButton)view.findViewById(R.id.lyric_loop);
        ib5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity MA = (MainActivity) getActivity();
                MA.rotateLoopMode();

            }
        });

        ImageButton ib6 = (ImageButton)view.findViewById(R.id.lyric_previous);
        ib6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    MainActivity MA = (MainActivity) getActivity();
                    MA.prevOrNext(-1);
            }
        });

        ImageButton ib7 = (ImageButton)view.findViewById(R.id.lyric_next);
        ib7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity MA = (MainActivity) getActivity();
                MA.prevOrNext(1);
            }
        });

        return view;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        //Log.d("■", "transit " +transit + "enter "+ enter +" nextAnim"+ nextAnim);
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            //if (enter) {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_bottom);
            //} else {
            //    return AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom);
            //}
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            //if (enter) {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom);
            //} else {
           //     return AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_out_bottom);
            //}
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    public void changeLoopIcon(int mLoop){
        switch (mLoop){
            case 0:
                ib5.setImageResource(R.drawable.ic_action_repeat);
                break;
            case 1:
                ib5.setImageResource(R.drawable.ic_action_repeat_1);
                break;
            case 2:
                ib5.setImageResource(R.drawable.ic_action_repeat_h);
                break;
        }
    }

    public void changeSong(String mTitle, String mLyric, String mList){
        tv0.setText(mTitle);
        tv.setText(mLyric);
        tv1.setText(Html.fromHtml(mList));
    }

}
